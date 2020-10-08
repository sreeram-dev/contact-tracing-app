# -*- coding:utf-8 -*-


import requests

from typing import List

from ens.diagnosis.repo import DiagnosisKeyRepository
from ens.diagnosis.constants import VERIFY_TAN_URI


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
