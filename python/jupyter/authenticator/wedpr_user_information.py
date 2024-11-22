# -*- coding: utf-8 -*-
import json
from typing import Any


class BaseObject:
    def set_params(self, **params: Any):
        for key, value in params.items():
            setattr(self, key, value)
            if hasattr(self, f"{key}"):
                setattr(self, f"{key}", value)
        return self


class WeDPRUserInformation(BaseObject):
    def __init__(self, user_name=None, role_name=None, **params):
        self.username = user_name
        self.roleName = role_name
        self.set_params(**params)

    @staticmethod
    def decode(json_str):
        if json_str is None or len(json_str) == 0:
            return None
        user_info_dict = json.loads(json_str)
        return WeDPRUserInformation(**user_info_dict)
