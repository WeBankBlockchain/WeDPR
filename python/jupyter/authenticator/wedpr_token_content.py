# -*- coding: utf-8 -*-
import json
from .wedpr_user_information import WeDPRUserInformation


class WeDPRTokenContent:
    USER_TOKEN_CLAIM = "user"

    def __init__(self, token_content):
        self._token_content = token_content

    def get_token_content(self):
        return self._token_content

    def set_token_content(self, _token_content):
        self._token_content = _token_content

    def get_token_content_by_key(self, key):
        if key in self._token_content:
            return self._token_content[key]
        return None

    def get_user_information(self) -> WeDPRUserInformation:
        user_info = self.get_token_content_by_key(
            WeDPRTokenContent.USER_TOKEN_CLAIM)
        if user_info is None:
            return None
        return WeDPRUserInformation(**json.loads(user_info))

    @staticmethod
    def deserialize(token_payload):
        return WeDPRTokenContent(token_payload)
