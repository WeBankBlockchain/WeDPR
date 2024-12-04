#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
from wedpr_builder.common import utilities


class BinaryGenerator:
    """
    generate the binary
    """
    def generate_binary(binary_path, dst_path):
        if os.path.exists(binary_path) is False:
            utilities.log_error(
                "The specified binary %s not exists!" % binary_path)
            return False
        # the binary has already been generated
        if os.path.exists(dst_path) is True:
            return True
        utilities.mkfiledir(dst_path)
        command = "cp %s %s" % (binary_path, dst_path)
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            utilities.log_error("copy binary from %s to %s failed, error: %s") % (
                binary_path, dst_path, output)
            return False
        return True
