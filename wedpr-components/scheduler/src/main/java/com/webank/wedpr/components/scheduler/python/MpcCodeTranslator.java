package com.webank.wedpr.components.scheduler.python;

import com.webank.wedpr.common.utils.WeDPRException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpcCodeTranslator {

    private static final Logger logger = LoggerFactory.getLogger(MpcCodeTranslator.class);

    public static final String MPC_CODE_TRANSLATOR_PYTHON_SCRIPT =
            "# generator main\n"
                    + "import sys\n"
                    + "import os\n"
                    + "import traceback\n"
                    + "\n"
                    + "current_file_path = os.path.abspath(__file__)\n"
                    + "current_file_real_path = os.path.realpath(current_file_path)\n"
                    + "current_dir = os.path.dirname(current_file_real_path)\n"
                    + "\n"
                    + "sys.path.append(current_dir)\n"
                    + "\n"
                    + "from mpc_generator.generator import CodeGenerator\n"
                    + "\n"
                    + "if len(sys.argv) <= 1:\n"
                    + "    print(\"Usage: python generator_main.py <sql>\")\n"
                    + "    sys.exit(1)\n"
                    + "\n"
                    + "sql = ' '.join(sys.argv[1:])\n"
                    + "\n"
                    + "# sql = \"SELECT 3*(s1.field3 + s2.field3) - s0.field3 AS r0, \\\n"
                    + "#                   (s0.field1 + s2.field1) / 2 * s1.field1 AS r1\\\n"
                    + "#               FROM (source0 AS s0\\\n"
                    + "#                   INNER JOIN source1 AS s1 ON s0.id = s1.id)\\\n"
                    + "#               INNER JOIN source2 AS s2 ON s0.id = s2.id;\"\n"
                    + "\n"
                    + "print (\"## original SQL => \" + str(sql))\n"
                    + "\n"
                    + "try:\n"
                    + "    code_gen = CodeGenerator(sql)\n"
                    + "    mpc_content = code_gen.sql_to_mpc_code()\n"
                    + "    print(mpc_content)\n"
                    + "    sys.exit(0)\n"
                    + "except Exception as e:\n"
                    + "    traceback.print_exc()\n"
                    + "    sys.exit(1)";

    public static String translateSqlToMpcCode(String sql)
            throws WeDPRException, IOException, InterruptedException {

        PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();

        String tempDirPath = System.getProperty("java.io.tmpdir");
        String pythonScriptFile = tempDirPath + File.separator + UUID.randomUUID() + ".py";

        logger.info("temp mpc generator python script file path: {}", pythonScriptFile);
        Path path = Paths.get(pythonScriptFile);
        try {

            Files.write(path, MPC_CODE_TRANSLATOR_PYTHON_SCRIPT.getBytes(StandardCharsets.UTF_8));

            return pythonScriptExecutor.executeScript(
                    pythonScriptFile, Collections.singletonList(sql));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    public static void main(String[] args)
            throws WeDPRException, IOException, InterruptedException {

        String sql = "select 2 * source0.field0 * source1.field0 from source0,source1;";
        if (args.length > 0) {
            sql = args[0];
        }

        System.out.println("# " + sql);

        String mpcCode = MpcCodeTranslator.translateSqlToMpcCode(sql);

        System.out.println(mpcCode);
    }
}
