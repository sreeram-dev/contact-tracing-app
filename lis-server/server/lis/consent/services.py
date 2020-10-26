# -*- coding:utf-8 -*-

from lis.consent.models import ConsentForm
from lis.consent.repo import ConsentRepository
from lis.consent.errors import ConsentNotFound

from lis.patient.repo import PatientRepository
from lis.patient.errors import PatientNotFound


class ConsentService(object):
    """Consent Service
    """
    consent_repo = ConsentRepository()
    patient_repo = PatientRepository()

    def grant_consent(self, uuid: str, host: str) -> ConsentForm:
        """Grant consent for uuid and host
        """
        patient = self.patient_repo.find_by_uuid(uuid)
        if not patient:
            raise PatientNotFound(f'uuid: {uuid} is not registered')
        return self.consent_repo.insert(uuid, host)

    def revoke_consent(self, uuid: str, host: str) -> ConsentForm:
        """Revoke consent for uuid and host
        """
        self.authenticate_consent(uuid, host)

        return self.consent_repo.revoke(uuid, host)

    def authenticate_consent(self, uuid: str, host: str) -> ConsentForm:
        """Authenticate Consent for UUID and HOST
        """
        patient = self.patient_repo.find_by_uuid(uuid)
        if not patient:
            raise PatientNotFound(f'uuid: {uuid} is not registered')

        consent = self.consent_repo.get(uuid, host)
        if not consent:
            raise ConsentNotFound(f'valid consent does not exist for uuid: \
                                  {uuid} and host: {host}')

        return consent
