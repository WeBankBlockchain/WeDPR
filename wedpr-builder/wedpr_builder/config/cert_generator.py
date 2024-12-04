#!/usr/bin/python
# -*- coding: UTF-8 -*-
from wedpr_builder.common import utilities


class CertGenerator:
    """
    the cert generator
    """
    def generate_ca_cert(sm_mode, ca_cert_path):
        """
        generate the ca cert
        """
        use_sm = "false"
        if sm_mode is True:
            use_sm = "true"
        command = "bash %s generate_ca_cert %s %s" % (
            utilities.ServiceInfo.cert_generation_script_path, use_sm, ca_cert_path)
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            utilities.log_error(
                "* generate ca cert error, sm_mode: %d, ca cert path: %s, error: %s" % (sm_mode, ca_cert_path, output))
            return False
        utilities.log_info(
            "* generate ca cert success, sm_mode: %d, ca cert path: %s" % (sm_mode, ca_cert_path))
        return True

    def generate_node_cert(sm_mode, ca_cert_path, node_cert_path):
        """
        generate the node cert
        """
        use_sm = "false"
        if sm_mode is True:
            use_sm = "true"
        command = "bash %s generate_node_cert %s %s %s" % (
            utilities.ServiceInfo.cert_generation_script_path, use_sm, ca_cert_path, node_cert_path)
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            utilities.log_error("* generate node cert error, sm_mode: %d, ca cert path: %s, node cert path: %s" %
                                (sm_mode, ca_cert_path, node_cert_path))
            return False
        utilities.log_info("* generate the node cert success, sm_mode: %d, ca cert path: %s, node cert path: %s" %
                           (sm_mode, ca_cert_path, node_cert_path))
        return True

    def generate_private_key(output_path):
        command = "bash %s generate_private_key_for_psi_server %s" % (
            utilities.ServiceInfo.cert_generation_script_path, output_path)
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            utilities.log_error(
                "* generate private key error, output_path: %s" % output_path)
            return (False, output)
        utilities.log_info(
            "* generate private_key success, path: %s, public_key: %s" % (output_path, output))
        return (True, output)
