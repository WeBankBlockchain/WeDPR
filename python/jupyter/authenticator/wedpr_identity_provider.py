"""
A JupyterHub authenticator class for use with WeDPR as an identity provider.
"""
from jupyter_server.auth.identity import IdentityProvider
from jupyter_server.auth.identity import User
import jwt
from .wedpr_token_content import WeDPRTokenContent
from tornado import web
from traitlets import Unicode, default
import os


class WeDPRIdentityProvider(IdentityProvider):
    """Authenticate local UNIX users with WeDPR-Auth"""
    AUTH_TOKEN_FIELD = "Authorization"
    AUTH_ALGORITHMS = ["HS256", "HS512", "HS384"]

    auth_secret = Unicode("<generated>",
                          help="auth_secret").tag(config=True)
    cookie_secret_file = Unicode(
        'jupyterlab_authorization_secret', help="""File in which to store the authorization secret."""
    ).tag(config=True)

    @default("cookie_secret_file")
    def _cookie_secret_file_default(self):
        if os.getenv("JUPYTER_AUTH_SECRET"):
            return os.getenv("JUPYTER_AUTH_SECRET")
        return "jupyter_lab_secret"

    @default("auth_secret")
    def _auth_secret_default(self):
        secret_file = os.path.abspath(
            os.path.expanduser(self.cookie_secret_file))
        if os.path.exists(secret_file):
            self.log.info(f"init auth secret, load from: {secret_file}")
            with open(secret_file) as f:
                return f.read().strip()
        return None

    def get_user(self, handler: web.RequestHandler):
        """Authenticate with jwt token, and return the username if login is successful.
        Return None otherwise.
        """
        try:
            token = handler.request.headers.get(
                WeDPRIdentityProvider.AUTH_TOKEN_FIELD, "")
        except KeyError as e:
            self.log.warning(
                f"WeDPR auth failed for no authorization inforamtion defined! error: {e}")
            return None
        try:
            token_payload = jwt.decode(
                token, self.auth_secret, WeDPRIdentityProvider.AUTH_ALGORITHMS)
            user_info = WeDPRTokenContent.deserialize(token_payload)
        except Exception as e:
            self.log.warning(
                f"WeDPR auth failed for jwt verify failed! error: {e}")
            return None
        if user_info is None:
            return None
        user_name = user_info.get_user_information().username
        self.log.info(f"WeDPR auth success, username: {user_name}")
        return User(username=user_name)
