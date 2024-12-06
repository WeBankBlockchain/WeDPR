import sys

from setuptools import find_packages, setup
from setuptools.command.bdist_egg import bdist_egg


class bdist_egg_disabled(bdist_egg):
    """Disabled version of bdist_egg

    Prevents setup.py install from performing setuptools' default easy_install,
    which it should never ever do.
    """

    def run(self):
        sys.exit(
            "Aborting implicit building of eggs. Use `pip install .` to install from source."
        )


setup_args = dict(
    name='wedpr_mpc_generator',
    packages=find_packages(),
    version="1.0.0.dev-20241206",
    description="wedpr-mpc-generator: The mpc generator for WeDPR",
    long_description_content_type="text/markdown",
    author="WeDPR Development Team",
    author_email="wedpr@webank.com",
    url="https://github.com/WeBankBlockchain/WeDPR",
    license="Apache-2.0",
    platforms="Linux, Mac OS X, Windows",
    keywords=['Interactive', 'Interpreter', 'Shell', 'Web'],
    python_requires=">=3.8",
    include_package_data=True,
    classifiers=[
        'Intended Audience :: Developers',
        'Intended Audience :: System Administrators',
        'Intended Audience :: Science/Research',
        'Programming Language :: Python',
        'Programming Language :: Python :: 3',
    ],
)

setup_args['cmdclass'] = {
    'bdist_egg': bdist_egg if 'bdist_egg' in sys.argv else bdist_egg_disabled,
}

setup_args['install_requires'] = install_requires = []
with open('requirements.txt') as f:
    for line in f.readlines():
        req = line.strip()
        if not req or req.startswith(('-e', '#')):
            continue
        install_requires.append(req)


def main():
    setup(**setup_args)


if __name__ == '__main__':
    main()
