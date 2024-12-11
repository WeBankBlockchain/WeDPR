#!/usr/bin/env python
# Copyright (c) Jupyter Development Team.
# Distributed under the terms of the Modified BSD License.
# -----------------------------------------------------------------------------
# Minimal Python version sanity check (from IPython/Jupyterhub)
# -----------------------------------------------------------------------------

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
    name='wedpr-authenticator',
    packages=find_packages(),
    version="3.0.0-20241124",
    description="WeDPR Authenticator: Authenticate JupyterHub users with wedpr providers",
    long_description_content_type="text/markdown",
    author="WeDPR Team",
    author_email="jupyter@googlegroups.com",
    url="https://jupyter.org",
    license="BSD",
    platforms="Linux, Mac OS X, Windows",
    keywords=['Interactive', 'Interpreter', 'Shell', 'Web'],
    python_requires=">=3.8",
    include_package_data=True,
    entry_points={
        'jupyter_server.IdentityProviders': [
            'wedpr-identity-provider = authenticator.wedpr_identity_provider.WeDPRIdentityProvider'
        ],
    },
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
