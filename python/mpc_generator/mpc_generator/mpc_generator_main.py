# generator main
import sys
import os
import traceback

current_file_path = os.path.abspath(__file__)
current_file_real_path = os.path.realpath(current_file_path)
current_dir = os.path.dirname(current_file_real_path)

sys.path.append(current_dir)

from mpc_generator.generator import CodeGenerator

if len(sys.argv) <= 1:
    print("Usage: python generator_main.py <sql>")
    sys.exit(1)

sql = ' '.join(sys.argv[1:])

# sql = "SELECT 3*(s1.field3 + s2.field3) - s0.field3 AS r0, \
#                   (s0.field1 + s2.field1) / 2 * s1.field1 AS r1\
#               FROM (source0 AS s0\
#                   INNER JOIN source1 AS s1 ON s0.id = s1.id)\
#               INNER JOIN source2 AS s2 ON s0.id = s2.id;"

print ("## original SQL => " + str(sql))

try:
    code_gen = CodeGenerator(sql)
    mpc_content = code_gen.sql_to_mpc_code()
    print(mpc_content)
    sys.exit(0)
except Exception as e:
    traceback.print_exc()
    sys.exit(1)

