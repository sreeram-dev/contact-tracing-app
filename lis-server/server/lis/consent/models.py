# -*- coding:utf-8 -*-

from datetime import datetime, timezone

from fireo.models import Model

from fireo.fields import TextField
from fireo.fields import DateTime
from lis.utils import get_collection_name


class ConsentForm(Model):
    """Consent given to host to fetch uuid from user
    """
    # UUID of the user for whom access to consent is given
    uuid = TextField(column_name='uuid', required=True)
    host = TextField(column_name='host', required=True)
    expired_at = DateTime(column_name='expired_at', required=True)
    revoked_at = DateTime(column_name='revoked_at', default=None)

    class Meta:
        collection_name = get_collection_name('consent')

    @property
    def is_expired(self) -> bool:
        now = datetime.utcnow().replace(tzinfo=timezone.utc)
        if self.expired_at < now:
            return True
        return False

    @property
    def is_revoked(self) -> bool:
        if self.revoked_at is not None:
            return True
        return False
