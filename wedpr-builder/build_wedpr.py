#!/usr/bin/python
# -*- coding: UTF-8 -*-
from wedpr_builder.controller import commandline_helper
from wedpr_builder.common import utilities
import traceback


def main():
    try:
        args = commandline_helper.parse_command()
        commandline_helper.execute_command(args)
    except Exception as error:
        utilities.log_error("%s" % error)
        traceback.print_exc()


if __name__ == "__main__":
    main()
