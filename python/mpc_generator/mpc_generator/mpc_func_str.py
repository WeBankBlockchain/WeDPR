import os

FILE_PATH = os.path.abspath(__file__)

CURRENT_PATH = os.path.abspath(os.path.dirname(FILE_PATH) + os.path.sep + ".")

AGGR_FUNC_SAMPLE_PATH = f"{CURRENT_PATH}{os.sep}mpc_sample{os.sep}aggr_func_only.mpc"
GROUP_BY_SAMPLE_PATH = f"{CURRENT_PATH}{os.sep}mpc_sample{os.sep}aggr_func_with_group_by.mpc"

FUNC_COMPUTE_SUM_NAME = 'compute_sum'
FUNC_COMPUTE_COUNT_NAME = 'compute_count'
FUNC_COMPUTE_AVG_NAME = 'compute_avg'
FUNC_COMPUTE_MAX_NAME = 'compute_max'
FUNC_COMPUTE_MIN_NAME = 'compute_min'

FUNC_COMPUTE_GROUP_BY_INDEXES_NAME = 'compute_group_by_indexes'
FUNC_COMPUTE_SUM_WITH_GROUP_BY_NAME = 'compute_sum_with_group_by'
FUNC_COMPUTE_COUNT_WITH_GROUP_BY_NAME = 'compute_count_with_group_by'
FUNC_COMPUTE_AVG_WITH_GROUP_BY_NAME = 'compute_avg_with_group_by'
FUNC_COMPUTE_MAX_WITH_GROUP_BY_NAME = 'compute_max_with_group_by'
FUNC_COMPUTE_MIN_WITH_GROUP_BY_NAME = 'compute_min_with_group_by'

with open(AGGR_FUNC_SAMPLE_PATH, "r") as file:
    AGGR_FUNC_SAMPLE_STR = file.read()

with open(GROUP_BY_SAMPLE_PATH, "r") as file:
    GROUP_BY_SAMPLE_STR = file.read()


def get_body_str_by_name(start_str, end_str, sql_pattern):
    if sql_pattern == 1:
        source_str = AGGR_FUNC_SAMPLE_STR
    elif sql_pattern == 2:
        source_str = GROUP_BY_SAMPLE_STR
    else:
        return ''

    start_index = source_str.find(start_str)
    source_str = source_str[start_index:]

    end_index = source_str.find(end_str) + len(end_str)
    return source_str[:end_index]


def get_func_str_by_name(func_name, sql_pattern):
    start_str = f"def {func_name}"
    end_str = "\n\n\n"
    return get_body_str_by_name(start_str, end_str, sql_pattern)


FUNC_COMPUTE_SUM = get_func_str_by_name(FUNC_COMPUTE_SUM_NAME, 1)
FUNC_COMPUTE_COUNT = get_func_str_by_name(FUNC_COMPUTE_COUNT_NAME, 1)
FUNC_COMPUTE_AVG = get_func_str_by_name(FUNC_COMPUTE_AVG_NAME, 1)
FUNC_COMPUTE_MAX = get_func_str_by_name(FUNC_COMPUTE_MAX_NAME, 1)
FUNC_COMPUTE_MIN = get_func_str_by_name(FUNC_COMPUTE_MIN_NAME, 1)

GROUP_BY_GLOBAL_VARIABLE = get_body_str_by_name("# matrix of indexes", "\n\n\n", 2)

FUNC_COMPUTE_GROUP_BY_INDEXES = get_func_str_by_name(FUNC_COMPUTE_GROUP_BY_INDEXES_NAME, 2)
FUNC_COMPUTE_SUM_WITH_GROUP_BY = get_func_str_by_name(FUNC_COMPUTE_SUM_WITH_GROUP_BY_NAME, 2)
FUNC_COMPUTE_COUNT_WITH_GROUP_BY = get_func_str_by_name(FUNC_COMPUTE_COUNT_WITH_GROUP_BY_NAME, 2)
FUNC_COMPUTE_AVG_WITH_GROUP_BY = get_func_str_by_name(FUNC_COMPUTE_AVG_WITH_GROUP_BY_NAME, 2)
FUNC_COMPUTE_MAX_WITH_GROUP_BY = get_func_str_by_name(FUNC_COMPUTE_MAX_WITH_GROUP_BY_NAME, 2)
FUNC_COMPUTE_MIN_WITH_GROUP_BY = get_func_str_by_name(FUNC_COMPUTE_MIN_WITH_GROUP_BY_NAME, 2)
