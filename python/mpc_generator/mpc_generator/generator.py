from enum import Enum

import os
import sys

current_file_path = os.path.abspath(__file__)
current_file_real_path = os.path.realpath(current_file_path)

current_dir = os.path.dirname(current_file_real_path)
parent_dir = os.path.dirname(current_dir)

sys.path.append(current_dir)
sys.path.append(parent_dir)
# print(sys.path)

import sqlparse
import sqlvalidator
from sqlparse.exceptions import SQLParseError
from sqlparse.sql import Comparison, Identifier, Function
from sqlparse.tokens import Punctuation, Operator, Name, Token

from mpc_generator import mpc_func_str
from mpc_generator.mpc_exception import MpcCodeGenException, MpcCodeGenErrorCode

class SqlPattern(Enum):
    BASIC_ARITH_OPE = 1
    AGGR_FUNC_ONLY = 2
    AGGR_FUNC_WITH_GROUP_BY = 3


INDENT = "    "

SUPPORTED_KEYWORDS = [
    'SELECT',
    'FROM',
    'WHERE',
    'JOIN',
    'INNER JOIN',
    'ON',
    'AS',
    'GROUP BY',
    'COUNT',
    'SUM',
    'AVG',
    'MAX',
    'MIN',
]

VALUE_TYPE = 'pfix'

GROUP_BY_COLUMN_CODE = 'group_indexes_key[i]'

DISPLAY_FIELDS_FUNC = 'set_display_field_names'

DISPLAY_RESULT_FUNC = 'display_data'

PPC_RESULT_FIELDS_FLAG = 'result_fields'
PPC_RESULT_VALUES_FLAG = 'result_values'

class CodeGenerator:

    def __init__(self, sql_str):
        self.sql_str = sql_str

        # three patterns supported
        self.sql_pattern = SqlPattern.BASIC_ARITH_OPE

        # based on ID only
        self.need_run_psi = False

        # record dataset sources
        self.table_set = set()

        # 0: table number, 1: column index
        self.group_by_column = []

        # filter conditions
        self.condition_list = []

        self.result_fields = []

        self.max_column_index = []

    def sql_to_mpc_code(self):
        operators = self.pre_parsing()
        format_sql_str = sqlparse.format(self.sql_str, reindent=True, keyword_case='upper')
        mpc_str = self.generate_common_code(format_sql_str)
        mpc_str = self.generate_function_code(mpc_str)
        mpc_str = self.generate_result_calculation_code(operators, mpc_str)
        mpc_str = self.generate_result_print_code(mpc_str)
        mpc_str = self.generate_mpc_execution_code(mpc_str)
        mpc_str = self.replace_max_filed(mpc_str)
        return mpc_str

    def pre_parsing(self):
        try:
            if not sqlvalidator.parse(self.sql_str).is_valid():
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "bad sql statement")

            # format sql str
            format_sql_str = sqlparse.format(self.sql_str, reindent=True, keyword_case='upper')

            tokens = sqlparse.parse(format_sql_str)[0].tokens

            # warning unsupported keywords
            self.check_sql_tokens(tokens)

            # parse table aliases
            aliases = self.find_table_alias(tokens, {}, False)

            # recover table aliases
            new_sql_str = self.recover_table_name(tokens, aliases, '')
            format_new_sql_str = sqlparse.format(new_sql_str, reindent=True, keyword_case='upper')
            tokens = sqlparse.parse(format_new_sql_str)[0].tokens

            # parse filters (only 'id' based column alignment is supported currently)
            self.find_table_and_condition(tokens, False)

            # check table suffix and number of participants
            self.check_table()

            # ensure that all tables participate in alignment
            self.check_table_alignment(self.need_run_psi, len(self.table_set))

            self.max_column_index = [0 for _ in range(len(self.table_set))]

            self.check_sql_pattern(tokens)

            operators = self.extract_operators(format_new_sql_str)

            return operators
        except SQLParseError:
            raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "bad sql statement")

    def check_sql_tokens(self, tokens):
        for token in tokens:
            if token.is_keyword and token.value not in SUPPORTED_KEYWORDS:
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), f"keyword '{token.value}' not supported")
            if hasattr(token, 'tokens'):
                self.check_sql_tokens(token.tokens)

    def find_table_alias(self, tokens, aliases, after_from):
        end_current_round = False
        for i in range(len(tokens)):
            if after_from and tokens[i].value == 'AS':
                # find a alias
                end_current_round = True
                aliases[tokens[i + 2].value] = tokens[i - 2].value
        for i in range(len(tokens)):
            if tokens[i].value == 'FROM':
                after_from = True
            if after_from and not end_current_round and hasattr(tokens[i], 'tokens'):
                self.find_table_alias(tokens[i].tokens, aliases, after_from)
        return aliases

    def recover_table_name(self, tokens, aliases, format_sql_str):
        for i in range(len(tokens)):
            if tokens[i].value == 'AS' and tokens[i + 2].value in aliases:
                break
            elif not tokens[i].is_group:
                if tokens[i].value in aliases:
                    format_sql_str += aliases[tokens[i].value]
                else:
                    format_sql_str += tokens[i].value
            elif hasattr(tokens[i], 'tokens'):
                format_sql_str = self.recover_table_name(tokens[i].tokens, aliases, format_sql_str)
        return format_sql_str

    def find_table_and_condition(self, tokens, after_from):
        for token in tokens:
            if token.value == 'FROM':
                after_from = True
            if after_from:
                if type(token) == Comparison:
                    self.check_equal_comparison(token.tokens)
                    self.condition_list.append(token.value)
                if type(token) == Identifier and '.' not in token.value:
                    self.table_set.add(token.value)
                elif hasattr(token, 'tokens'):
                    self.find_table_and_condition(token.tokens, after_from)

    def check_equal_comparison(self, tokens):
        for i in range(len(tokens)):
            if tokens[i].value == '=':
                self.need_run_psi = True
            elif tokens[i].value == '.' and tokens[i + 1].value != 'id':
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(),
                                   f"only 'id' based column alignment is supported currently")
            elif hasattr(tokens[i], 'tokens'):
                self.check_equal_comparison(tokens[i].tokens)

    def check_table_alignment(self, has_equal_comparison, table_count):
        if has_equal_comparison:
            column = self.condition_list[0].split('=')[0].strip()
            table = column[0:column.find('.')]

            # all tables must be aligned
            self.equal_comparison_dfs(table, [0 for _ in range(len(self.condition_list))])
            if len(self.table_set) != table_count:
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "all tables must be aligned")

    def equal_comparison_dfs(self, table, flag_array):
        for i in range(len(self.condition_list)):
            if flag_array[i] == 0 and table in self.condition_list[i]:
                flag_array[i] = 1
                columns = self.condition_list[i].split('=')
                for column in columns:
                    table = column[0:column.find('.')].strip()
                    self.table_set.add(table)
                    self.equal_comparison_dfs(table, flag_array)

    def check_table(self):
        table_count = len(self.table_set)
        if table_count > 5 or table_count < 2:
            raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "source must be 2 to 5 parties")
        suffixes = set()
        for table in self.table_set:
            suffixes.add(table[-1])
        for i in range(table_count):
            if str(i) not in suffixes:
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "invalid suffix of table name")

    def update_max_field(self, table_number, field_number):
        if field_number > self.max_column_index[table_number]:
            self.max_column_index[table_number] = field_number

    def replace_max_filed(self, mpc_str):
        for i in range(len(self.max_column_index)):
            mpc_str = mpc_str.replace(f'$(source{i}_column_count)', str(self.max_column_index[i] + 1))
        return mpc_str

    def check_sql_pattern(self, tokens):
        for i in range(len(tokens)):
            if tokens[i].value == 'GROUP BY':
                self.sql_pattern = SqlPattern.AGGR_FUNC_WITH_GROUP_BY
                items = tokens[i + 2].value.split('.')
                self.group_by_column = [int(items[0][-1]), get_column_number(items[1])]
            elif type(tokens[i]) == Function:
                self.sql_pattern = SqlPattern.AGGR_FUNC_ONLY
            elif hasattr(tokens[i], 'tokens'):
                self.check_sql_pattern(tokens[i].tokens)

    def extract_operators(self, format_sql_str):
        start = format_sql_str.find("SELECT")
        end = format_sql_str.find("FROM")
        operators = format_sql_str[start + 6:end].split(',')
        for i in range(len(operators)):
            if ' AS ' in operators[i]:
                index = operators[i].find(' AS ')
                self.result_fields.append(operators[i][index + 4:].strip().strip('\n').strip('\'').strip('\"').strip())
                operators[i] = operators[i][:index].strip()
            else:
                self.result_fields.append(f"result{i}")
                operators[i] = operators[i].strip()
            if ' ' in self.result_fields[-1]:
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "result field cannot contain space")
        return operators

    def generate_common_code(self, format_sql_str):
        table_count = len(self.table_set)
        result_column_count = len(self.result_fields)

        if self.need_run_psi:
            mpc_str = '# PSI_OPTION=True\n\n'
        else:
            mpc_str = ''
        mpc_str += '# BIT_LENGTH = 128\n\n'
        mpc_str += '# This file is generated automatically by ams\n'
        mpc_str += f"'''\n{format_sql_str}\n'''\n\n"
        mpc_str += "from ppc import *\n\n"
        # mpc_str += "program.use_trunc_pr = True\n"
        # mpc_str += "program.use_split(3)\n"
        mpc_str += "n_threads = 8\n"
        mpc_str += f"value_type = {VALUE_TYPE}\n\n"
        if VALUE_TYPE == 'pfix':
            mpc_str += f"pfix.set_precision(16, 47)\n\n"

        for i in range(table_count):
            mpc_str += f"SOURCE{i} = {i}\n"
            mpc_str += f"source{i}_record_count = $(source{i}_record_count)\n"
            mpc_str += f"source{i}_column_count = $(source{i}_column_count)\n"
            mpc_str += f"source{i}_record = Matrix(source{i}_record_count, source{i}_column_count, value_type)\n\n"

        if self.sql_pattern == SqlPattern.BASIC_ARITH_OPE:
            mpc_str += "# basic arithmetic operation means that all parties have same number of record\n"
            mpc_str += "result_record = $(source0_record_count)\n"
            mpc_str += f"results = Matrix(result_record, {result_column_count}, value_type)\n\n\n"
        elif self.sql_pattern == SqlPattern.AGGR_FUNC_ONLY:
            mpc_str += f"results = Matrix(1, {result_column_count}, value_type)\n\n\n"
        elif self.sql_pattern == SqlPattern.AGGR_FUNC_WITH_GROUP_BY:
            mpc_str += "# group by means all parties have same number of record\n"
            mpc_str += "source_record_count = $(source0_record_count)\n"
            mpc_str += "result_record = cint(source_record_count)\n"
            mpc_str += f"results = Matrix(source_record_count, {result_column_count}, value_type)\n\n\n"

        mpc_str += "def read_data_collection(data_collection, party_id):\n"
        mpc_str += f"{INDENT}if data_collection.sizes[0] > 0:\n"
        mpc_str += f"{INDENT}{INDENT}data_collection.input_from(party_id)\n\n\n"

        return mpc_str

    def generate_function_code(self, mpc_str):
        if self.sql_pattern == SqlPattern.AGGR_FUNC_ONLY:
            mpc_str += mpc_func_str.FUNC_COMPUTE_SUM
            mpc_str += mpc_func_str.FUNC_COMPUTE_COUNT
            mpc_str += mpc_func_str.FUNC_COMPUTE_AVG
            mpc_str += mpc_func_str.FUNC_COMPUTE_MAX
            mpc_str += mpc_func_str.FUNC_COMPUTE_MIN
        elif self.sql_pattern == SqlPattern.AGGR_FUNC_WITH_GROUP_BY:
            mpc_str += mpc_func_str.GROUP_BY_GLOBAL_VARIABLE
            mpc_str += mpc_func_str.FUNC_COMPUTE_GROUP_BY_INDEXES
            mpc_str += mpc_func_str.FUNC_COMPUTE_SUM_WITH_GROUP_BY
            mpc_str += mpc_func_str.FUNC_COMPUTE_COUNT_WITH_GROUP_BY
            mpc_str += mpc_func_str.FUNC_COMPUTE_AVG_WITH_GROUP_BY
            mpc_str += mpc_func_str.FUNC_COMPUTE_MAX_WITH_GROUP_BY
            mpc_str += mpc_func_str.FUNC_COMPUTE_MIN_WITH_GROUP_BY
        return mpc_str

    def generate_result_calculation_code(self, operators, mpc_str):
        for i in range(len(operators)):
            tokens = sqlparse.parse(operators[i])[0].tokens
            participants_set = set()
            formula_str = self.generate_formula(tokens, '', participants_set)
            if len(participants_set) == 1:
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "disabled query pattern")

            if self.sql_pattern == SqlPattern.BASIC_ARITH_OPE or self.sql_pattern == SqlPattern.AGGR_FUNC_WITH_GROUP_BY:
                mpc_str += f"def calculate_result_{i}():\n" \
                           f"{INDENT}@for_range_opt(result_record)\n" \
                           f"{INDENT}def _(i):\n" \
                           f"{INDENT}{INDENT}results[i][{i}] = {formula_str}\n\n\n"
            elif self.sql_pattern == SqlPattern.AGGR_FUNC_ONLY:
                mpc_str += f"def calculate_result_{i}():\n" \
                           f"{INDENT}results[0][{i}] = {formula_str}\n\n\n"
        return mpc_str

    def generate_result_print_code(self, mpc_str):
        field_print_str = f"{PPC_RESULT_FIELDS_FLAG} = ['{self.result_fields[0]}'"
        for i in range(1, len(self.result_fields)):
            field_print_str += f", '{self.result_fields[i]}'"
        field_print_str += ']'

        if self.sql_pattern == SqlPattern.BASIC_ARITH_OPE or self.sql_pattern == SqlPattern.AGGR_FUNC_WITH_GROUP_BY:
            result_print_str = f"{PPC_RESULT_VALUES_FLAG} = [results[i][0].reveal()"
            for i in range(1, len(self.result_fields)):
                result_print_str += f", results[i][{i}].reveal()"
            result_print_str += ']'
            mpc_str += f"def print_results():\n" \
                       f"{INDENT}{field_print_str}\n" \
                       f"{INDENT}{DISPLAY_FIELDS_FUNC}({PPC_RESULT_FIELDS_FLAG})\n\n" \
                       f"{INDENT}@for_range_opt(result_record)\n" \
                       f"{INDENT}def _(i):\n" \
                       f"{INDENT}{INDENT}{result_print_str}\n" \
                       f"{INDENT}{INDENT}{DISPLAY_RESULT_FUNC}({PPC_RESULT_VALUES_FLAG})\n\n\n"
        elif self.sql_pattern == SqlPattern.AGGR_FUNC_ONLY:
            result_print_str = f"{PPC_RESULT_VALUES_FLAG} = [results[0][0].reveal()"
            for i in range(1, len(self.result_fields)):
                result_print_str += f", results[0][{i}].reveal()"
            result_print_str += ']'
            mpc_str += f"def print_results():\n" \
                       f"{INDENT}{field_print_str}\n" \
                       f"{INDENT}{DISPLAY_FIELDS_FUNC}({PPC_RESULT_FIELDS_FLAG})\n\n" \
                       f"{INDENT}{result_print_str}\n" \
                       f"{INDENT}{DISPLAY_RESULT_FUNC}({PPC_RESULT_VALUES_FLAG})\n\n\n"
        return mpc_str

    def generate_mpc_execution_code(self, mpc_str):
        mpc_str += 'def ppc_main():\n'
        for i in range(len(self.table_set)):
            mpc_str += f"{INDENT}read_data_collection(source{i}_record, SOURCE{i})\n"

        if self.sql_pattern == SqlPattern.AGGR_FUNC_WITH_GROUP_BY:
            mpc_str += f"\n{INDENT}compute_group_by_indexes(source{self.group_by_column[0]}_record, " \
                       f"{self.group_by_column[1]})\n\n"

        for i in range(len(self.result_fields)):
            mpc_str += f"{INDENT}calculate_result_{i}()\n"

        mpc_str += f"\n{INDENT}print_results()\n\n\n"

        mpc_str += "ppc_main()\n"
        return mpc_str

    def generate_formula(self, tokens, formula_str, participants_set):
        for token in tokens:
            if token.ttype == Punctuation \
                    or token.ttype == Operator \
                    or token.ttype == Token.Literal.Number.Integer \
                    or token.ttype == Token.Operator.Comparison:
                formula_str += token.value
            elif type(token) == Function:
                formula_str += self.handle_function(token)
            elif type(token) == Identifier and token.tokens[0].ttype == Name and len(token.tokens) >= 3:
                (table_number, field_number) = self.handle_basic_identifier(token)
                if self.sql_pattern == SqlPattern.AGGR_FUNC_WITH_GROUP_BY:
                    if table_number != self.group_by_column[0] or field_number != self.group_by_column[1]:
                        raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "bad sql statement")
                    self.update_max_field(table_number, field_number)
                    formula_str += GROUP_BY_COLUMN_CODE
                elif self.sql_pattern == SqlPattern.AGGR_FUNC_ONLY:
                    raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "disabled query pattern")
                elif self.sql_pattern == SqlPattern.BASIC_ARITH_OPE:
                    if token.value == token.parent.value:
                        raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(),
                                           "disabled query pattern")
                    self.update_max_field(table_number, field_number)
                    formula_str += f"source{table_number}_record[i][{field_number}]"
                    participants_set.add(table_number)
            elif hasattr(token, 'tokens'):
                formula_str = self.generate_formula(token.tokens, formula_str, participants_set)
        return formula_str

    def handle_function(self, token):
        tokens = token.tokens
        func_name = tokens[0].value
        (table_number, field_number) = self.handle_parenthesis(tokens[1])
        self.update_max_field(table_number, field_number)
        return self.func_to_formula(func_name, table_number, field_number)

    def func_to_formula(self, func, table_number, field_number):
        if self.sql_pattern == SqlPattern.AGGR_FUNC_ONLY:
            formula = {
                'COUNT': f"{mpc_func_str.FUNC_COMPUTE_COUNT_NAME}(source{table_number}_record_count)",
                'SUM': f"{mpc_func_str.FUNC_COMPUTE_SUM_NAME}(source{table_number}_record, "
                       f"source{table_number}_record_count, {field_number})",
                'AVG': f"{mpc_func_str.FUNC_COMPUTE_AVG_NAME}(source{table_number}_record, "
                       f"source{table_number}_record_count, {field_number})",
                'MAX': f"{mpc_func_str.FUNC_COMPUTE_MAX_NAME}(source{table_number}_record, "
                       f"source{table_number}_record_count, {field_number})",
                'MIN': f"{mpc_func_str.FUNC_COMPUTE_MIN_NAME}(source{table_number}_record, "
                       f"source{table_number}_record_count, {field_number})"
            }
        elif self.sql_pattern == SqlPattern.AGGR_FUNC_WITH_GROUP_BY:
            formula = {
                'COUNT': f"{mpc_func_str.FUNC_COMPUTE_COUNT_WITH_GROUP_BY_NAME}(i)",
                'SUM': f"{mpc_func_str.FUNC_COMPUTE_SUM_WITH_GROUP_BY_NAME}(source{table_number}_record,"
                       f" {field_number}, i)",
                'AVG': f"{mpc_func_str.FUNC_COMPUTE_AVG_WITH_GROUP_BY_NAME}(source{table_number}_record,"
                       f" {field_number}, i)",
                'MAX': f"{mpc_func_str.FUNC_COMPUTE_MAX_WITH_GROUP_BY_NAME}(source{table_number}_record,"
                       f" {field_number}, i)",
                'MIN': f"{mpc_func_str.FUNC_COMPUTE_MIN_WITH_GROUP_BY_NAME}(source{table_number}_record,"
                       f" {field_number}, i)"
            }
        else:
            formula = {}

        return formula.get(func, '')

    def handle_parenthesis(self, token):
        for token in token.tokens:
            if type(token) == Identifier:
                (table_number, field_number) = self.handle_basic_identifier(token)
                return table_number, field_number

    def handle_basic_identifier(self, token):
        tokens = token.tokens

        if tokens[0].value not in self.table_set:
            raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "table name not matched")
        if tokens[1].value != '.':
            raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), "invalid identifier")

        field_num = get_column_number(tokens[2].value)

        return int(tokens[0].value[-1]), field_num


def get_column_number(field_name):
    field_len = len(field_name)
    field_num = 0
    for i in range(field_len, 0, -1):
        try:
            int(field_name[i - 1:field_len])
        except ValueError:
            if i == field_len:
                raise MpcCodeGenException(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(),
                                   f"invalid field suffix of table column '{field_name}'")
            field_num = int(field_name[i:field_len])
            break
    return field_num
