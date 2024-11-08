#!/usr/bin/python
# -*- coding: utf-8 -*-
import os
import unittest

from mpc_generator.generator import CodeGenerator
from mpc_generator.mpc_exception import MpcCodeGenErrorCode, MpcCodeGenException

FILE_PATH = os.path.abspath(__file__)

CURRENT_PATH = os.path.abspath(os.path.dirname(FILE_PATH) + os.path.sep + ".")

BASIC_ARITH_OPE_PATH = f"{CURRENT_PATH}{os.sep}mpc_sample{os.sep}basic_arith_ope.mpc"
AGGR_FUNC_SAMPLE_PATH = f"{CURRENT_PATH}{os.sep}mpc_sample{os.sep}aggr_func_only.mpc"
GROUP_BY_SAMPLE_PATH = f"{CURRENT_PATH}{os.sep}mpc_sample{os.sep}aggr_func_with_group_by.mpc"

with open(BASIC_ARITH_OPE_PATH, "r") as file:
    BASIC_ARITH_OPE_STR = file.read()

with open(AGGR_FUNC_SAMPLE_PATH, "r") as file:
    AGGR_FUNC_SAMPLE_STR = file.read()

with open(GROUP_BY_SAMPLE_PATH, "r") as file:
    GROUP_BY_SAMPLE_STR = file.read()


class TestGenerator(unittest.TestCase):

    def test_bad_sql(self):
        try:
            sql = "select from a from b where c = d"
            code_generator = CodeGenerator(sql)
            code_generator.sql_to_mpc_code()
        except MpcCodeGenException as e:
            self.assertEqual(MpcCodeGenErrorCode.ALGORITHM_BAD_SQL.get_code(), e.code)

        sql = "select a0.f1 + b1.f1 from a0, b1 where a0.id=b1.id"
        code_generator = CodeGenerator(sql)
        self.assertIsNotNone(code_generator.sql_to_mpc_code())

    def test_unsupported_keyword(self):
        try:
            sql = "select s0.f1 + s1.f1 from s0, s1 where s0.f1 > 1 and s0.f1 < 10"
            code_generator = CodeGenerator(sql)
            code_generator.sql_to_mpc_code()
        except MpcCodeGenException as e:
            self.assertEqual("keyword 'AND' not supported", e.message)

    def test_disabled_query_pattern(self):
        try:
            sql = "select s0.f1, 3 + s1.f1 from s0, s1"
            code_generator = CodeGenerator(sql)
            code_generator.sql_to_mpc_code()
        except MpcCodeGenException as e:
            self.assertEqual("disabled query pattern", e.message)

        try:
            sql = "select s0.f1, s1.f1 + s1.f1 from s0, s1"
            code_generator = CodeGenerator(sql)
            code_generator.sql_to_mpc_code()
        except MpcCodeGenException as e:
            self.assertEqual("disabled query pattern", e.message)

    def test_basic_pattern(self):
        sql = "SELECT 3*(s1.field3 + s2.field3) - s0.field3 AS r0, \
                  (s0.field1 + s2.field1) / 2 * s1.field1 AS r1\
              FROM (source0 AS s0\
                  INNER JOIN source1 AS s1 ON s0.id = s1.id)\
              INNER JOIN source2 AS s2 ON s0.id = s2.id;"
        code_generator = CodeGenerator(sql)
        self.assertEqual(BASIC_ARITH_OPE_STR, code_generator.sql_to_mpc_code())

    def test_single_aggre_pattern(self):
        sql = "SELECT COUNT(s1.field3) + COUNT(s2.field3) AS r0,\
                  SUM(s1.field3) + COUNT(s0.field0) AS 'count',\
                  (MAX(s0.field1) + MAX(s2.field1)) / 2 AS r1,\
                  (AVG(s1.field2) + AVG(s2.field2)) / 2 AS r2,\
                  MIN(s1.field0) - MIN(s0.field0) AS r3\
               FROM (source0 AS s0\
                  INNER JOIN source1 AS s1 ON s0.id = s1.id)\
               INNER JOIN source2 AS s2 ON s0.id = s2.id;"
        code_generator = CodeGenerator(sql)
        self.assertEqual(AGGR_FUNC_SAMPLE_STR, code_generator.sql_to_mpc_code())

    def test_group_by_pattern(self):
        sql = "SELECT 3*s1.field4 AS r0,\
                   COUNT(s1.field4) AS 'count', \
                   AVG(s0.field1) * 2 + s1.field4 AS r1,\
                   (SUM(s0.field2) + SUM(s1.field2))/(COUNT(s1.field3) + 100/(MIN(s0.field1)+MIN(s1.field1))) + 10,\
                   MAX(s1.field1),\
                   MIN(s2.field2)\
               FROM (source0 AS s0\
                   INNER JOIN source1 AS s1 ON s0.id = s1.id)\
               INNER JOIN source2 AS s2 ON s0.id = s2.id\
               GROUP BY s1.field4;"
        print(sql)
        code_generator = CodeGenerator(sql)
        self.assertEqual(GROUP_BY_SAMPLE_STR, code_generator.sql_to_mpc_code())


if __name__ == '__main__':
    unittest.main(verbosity=1)
