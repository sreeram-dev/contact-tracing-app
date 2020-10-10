# -*- coding:utf-8 -*-

from datetime import datetime, timezone, timedelta

from lis.patient.models import PatientProfile
from lis.patient.repo import PatientRepository
from lis.patient.errors import ValidationError


class PatientService(object):
    """Business Service to cater for view calls
    """
    patient_repo = PatientRepository()

    def find_by_uuid(self, uuid: str) -> PatientProfile:
        """Check if UUID exists
        """
        return self.patient_repo.find_by_uuid(uuid)

    def register_uuid(self, uuid: str) -> PatientProfile:
        """Save the uuid and give patientProfile
        """
        profile = self.patient_repo.add_new_patient(uuid)

        return profile

    def set_diagnosis_status(
            self, uuid, recovered_status, positive_status) -> PatientProfile:

        profile = self.find_by_uuid(uuid)

        if recovered_status and not profile.is_positive:
            raise ValidationError('Patient is not diagnosed positive')

        if positive_status and profile.is_positive:
            raise ValidationError(
                'Patient has already been diagnosed positive')

        if not recovered_status and not positive_status:
            raise ValidationError('No status set')

        if positive_status:
            profile.is_positive = True
            profile.positive_at = datetime.utcnow().replace(
                tzinfo=timezone.utc)
        elif recovered_status:
            profile.is_recovered = True
            profile.recovered_at = datetime.utcnow().replace(
                tzinfo=timezone.utc)

        profile.update()
        return profile
