# -*- coding:utf-8 -*-

from urllib.parse import urlencode
from datetime import datetime, timedelta
from typing import List

from ens.diagnosis.models import TEKInfo


class DiagnosisKeyRepository(object):
    """Repository to store a diagnosis key
    """

    def insert_tek(self, tek: str, en_interval_number: int):
        """Stores the infected user tek by token_id and en_interval_number
        """
        tek_info = TEKInfo()
        tek_info.id = urlencode(dict(p=tek))
        tek_info.tek = tek
        tek_info.en_interval_number = en_interval_number
        tek_info.created_at = datetime.now()
        tek_info.save()

    def delete_teks_by_token_id(self, token_id: str):
        teks = TEKInfo.collection.filter(
            'token_id', '==', token_id).fetch()

        for tek in teks:
            tek.delete()

    def get_all_teks(self, days: int = 14) -> List[TEKInfo]:
        """Get all teks from latest days
        """
        now = datetime.now()
        minus_fourteen = now - timedelta(days=days)
        minus_fourteen = int(minus_fourteen.timestamp())
        teks = TEKInfo.collection.filter(
            'created_at', '>=', minus_fourteen).fetch()
        return teks
