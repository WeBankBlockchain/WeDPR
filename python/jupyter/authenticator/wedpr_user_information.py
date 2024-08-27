# -*- coding: utf-8 -*-
from dataclasses import dataclass
import dataclasses
from dataclass_wizard import JSONWizard
import inspect


@dataclass
class WeDPRGroupInfo(JSONWizard):
    groupId: str
    groupName: str
    groupAdminName: str


@dataclass
class WeDPRUserInformation:
    username: str
    roleName: str
    permissions: list

    def __init__(self, **kwargs):
        names = set([f.name for f in dataclasses.fields(self)])
        for k, v in kwargs.items():
            if k in names:
                setattr(self, k, v)
