# -*- coding:utf-8 -*-

from datetime import datetime, timedelta, timezone
from lis.consent.models import ConsentForm


class ConsentRepository(object):

    def insert(self, uuid: str, host: str) -> ConsentForm:
        """Create a consent for uuid and host
        """
        expiry = datetime.utcnow() + timedelta(days=14)
        expiry = expiry.replace(tzinfo=timezone.utc)
        now = datetime.utcnow().replace(tzinfo=timezone.utc)

        consent = self.get(uuid, host)
        # revoke the old consent
        if consent:
            consent.revoked_at = now
            consent.update()
        new_consent = ConsentForm(uuid=uuid, host=host, expired_at=expiry)
        new_consent.save()
        return new_consent

    def get(self, uuid: str, host: str) -> ConsentForm:
        """Get the consent for uuid and host
        """
        now = datetime.utcnow().replace(tzinfo=timezone.utc)
        query = ConsentForm.collection.filter('uuid', '==', uuid)
        query = query.filter('host', '==', host)
        query = query.filter('expired_at', '>=', now)
        query = query.filter('revoked_at', '==', None)

        return query.get()

    def revoke(self, uuid: str, host: str) -> ConsentForm:
        """Revoke the consent
        """
        # Get unexpired and unrevoked consents
        now = datetime.utcnow().replace(tzinfo=timezone.utc)
        consent = self.get(uuid, host)
        if consent:
            consent.revoked_at = now
            consent.update()
            return consent
