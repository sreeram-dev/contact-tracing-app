# -*- coding:utf-8 -*-

import uuid
from datetime import datetime, timezone, timedelta
from covid.tan.models import TANInfo


class TANRepository(object):
    """Repository wrapper for TAN
    """

    def create_tan_for_token(self, token: str) -> TANInfo:
        """Create TAN for token
        """
        tan = uuid.uuid4().hex[:8]
        expired_at = datetime.utcnow() + timedelta(hours=5)
        expired_at = expired_at.replace(tzinfo=timezone.utc)

        tan_info = TANInfo(tan=tan, token=token, expired_at=expired_at)
        tan_info.save()

        return tan_info

    def find_by_tan(self, tan: str) -> TANInfo:
        """Get TAN for token
        """
        tan_info = TANInfo.collection.filter('tan', '==', tan).get()
        return tan_info
