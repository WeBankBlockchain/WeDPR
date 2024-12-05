#!/usr/bin/python
# -*- coding: UTF-8 -*-
from wedpr_builder.common import constant
from wedpr_builder.common import utilities
from gmssl import func


class UserJWTConfig:
    def __init__(self):
        self.user_jwt_sk = func.random_hex(64)
        self.user_jwt_session = utilities.generate_random_str()

    def to_properties(self) -> {}:
        props = {}
        props.update(
            {constant.ConfigProperities.USER_JWT_SK: self.user_jwt_sk})
        props.update(
            {constant.ConfigProperities.USER_JWT_SESSION: self.user_jwt_session})
        return props
