# -*- coding:utf-8 -*-

import requests

from datetime import datetime, timezone, timedelta
from typing import List

from ens.diagnosis.repo import DiagnosisKeyRepository
from ens.diagnosis.constants import VERIFY_TAN_URI
from ens.providers.db import get_firestore_client
from ens.utils import get_collection_name


class DiagnosisKeyService(object):
    """Service to create diagnosis keys
    """
    diag_repo = DiagnosisKeyRepository()

    def call_verify_tan(self, tan: str) -> dict:

        if tan is None:
            return False

        r = requests.post(VERIFY_TAN_URI, data={'tan': tan})
        res = r.json()
        return res

    def upload_teks(self, teks: List[dict]):
        """Store the uploaded teks if they are unique
        """
        for tek in teks:
            self.diag_repo.insert_tek(tek['tek'], tek['en_interval_number'])

    def download_teks(self) -> List[dict]:
        """Download the teks
        """
        client = get_firestore_client()
        timestamp = datetime.now() - timedelta(days=14)
        query = client.collection(get_collection_name('tek_info')).where(
            'created_at', '>=', timestamp).select(
                ['tek', 'en_interval_number'])

        teks = list()
        for doc in query.stream():
            tek_info = doc.to_dict()
            teks.append(tek_info)
        return teks

    def delete_old_teks(self):
        """Delete the old teks
        """
        now = datetime.utcnow().replace(tzinfo=timezone.utc)
        expired_at = now - timedelta(days=14)
        epoch_timestamp = int(expired_at.timestamp())

        self.diag_repo.delete_from_timesamp(epoch_timestamp)
