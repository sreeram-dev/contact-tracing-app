# -*- coding:utf-8 -*-

from datetime import datetime, timezone, timedelta

from covid.app import APP_MODE
from covid.tan.repo import TANRepository
from covid.tan.models import TANInfo
from covid.tan.errors import TANValidationError


class TANService(object):
    tan_repo = TANRepository()

    def generate_tan_for_token(self, token: str) -> TANInfo:
        """Generate TAN for token
        """
        tan_info = self.tan_repo.create_tan_for_token(token)
        return tan_info

    def validate_tan(self, tan: str):
        """Validate the tan for the token
        """
        tan_info = self.tan_repo.find_by_tan(tan)
        if tan_info is None:
            raise ValueError('Tan does not exist for tan: ' + tan)

        if tan_info.is_verified:
            raise TANValidationError(
                'TAN has been processed already', error_code=400)

        now = datetime.utcnow().replace(tzinfo=timezone.utc)
        if tan_info.expired_at < now:
            raise TANValidationError(
                'TAN has expired. tan: ' + tan_info.get_tan())

        if APP_MODE != 'dev':
            tan_info.is_verified = True
            tan_info.update()
