# -*- coding: utf-8 -*-
from enum import Enum, unique


@unique
class MpcCodeGenErrorCode(Enum):
    SUCCESS = {0: 'success'}
    INTERNAL_ERROR = {10000: "internal error"}

    ALGORITHM_PARSE_ERROR = {10400: 'parse algorithm failed'}
    ALGORITHM_EXIST_ERROR = {10401: 'algorithm already existed!'}
    ALGORITHM_DELETE_ERROR = {10402: 'algorithm already deleted!'}
    ALGORITHM_TYPE_ERROR = {10403: 'algorithm type is not existed!'}
    ALGORITHM_COMPILE_ERROR = {10410: 'compile mpc algorithm error'}
    ALGORITHM_BAD_SQL = {10411: 'bad sql'}
    ALGORITHM_PPC_CONFIG_ERROR = {10412: 'parse algorithm config error'}
    ALGORITHM_PPC_MODEL_ALGORITHM_NAME_ERROR = {
        10413: 'algorithm subtype not found'}
    ALGORITHM_PPC_MODEL_OUTPUT_NUMBER_ERROR = {10414: 'output number error'}
    ALGORITHM_PPC_MODEL_LAYERS_ERROR = {
        10415: 'layers attribute should not be set'}
    ALGORITHM_MPC_SYNTAX_CHECK_ERROR = {10416: 'check ppc mpc syntax error'}
    ALGORITHM_PPC_MODEL_OUTPUT_NUMBER_ERROR_TEMP = {
        10417: 'output number should be set 1'}
    ALGORITHM_PPC_MODEL_PARTICIPANTS_ERROR_TEMP = {
        10418: 'participants should be greater or equal to 2'}
    ALGORITHM_PPC_MODEL_TEST_DATASET_PERCENTAGE_ERROR = {
        10419: 'test_dataset_percentage should be set in (0, 0.5]'}
    ALGORITHM_PPC_MODEL_EPOCHS_ERROR = {
        10420: 'epochs should be set in [1, 5]'}
    ALGORITHM_PPC_MODEL_BATCH_SIZE_ERROR = {
        10421: 'batch_size should be set [1, min(128, max_train_dataset_size)]'}
    ALGORITHM_PPC_MODEL_THREADS_ERROR = {
        10422: 'threads should be set in [1,8]'}
    ALGORITHM_PPC_MODEL_OPTIMIZER_ERROR = {10423: 'optimizer not found'}
    ALGORITHM_PPC_MODEL_LEARNING_RATE_ERROR = {
        10424: 'learning rate should be set in (0, 1)'}
    ALGORITHM_PPC_MODEL_LAYERS_ERROR2 = {
        10425: 'Conv2d layer should not be the first layer in HeteroNN'}
    ALGORITHM_NOT_EXIST_ERROR = {10426: 'algorithm does not exist!'}
    ALGORITHM_PPC_MODEL_TREES_ERROR = {
        10427: 'num_trees should be set in [1, 300]'}
    ALGORITHM_PPC_MODEL_DEPTH_ERROR = {
        10428: 'max_depth should be set in [1, 10]'}

    def get_code(self):
        return list(self.value.keys())[0]

    def get_error_code(self):
        return list(self.value.keys())[0]

    def get_msg(self):
        return list(self.value.values())[0]

    def get_message(self):
        return list(self.value.values())[0]


class MpcCodeGenException(Exception):

    def __init__(self, code, message):
        Exception.__init__(self)
        self.code = code
        self.message = message

    def to_dict(self):
        return {'errorCode': self.code, 'message': self.message}

    def get_code(self):
        return self.code

    def __str__(self):
        return self.message

    @classmethod
    def by_ppc_error_code(cls, ppc_error_code):
        cls.code = ppc_error_code.get_code()
        cls.message = ppc_error_code.get_msg()
