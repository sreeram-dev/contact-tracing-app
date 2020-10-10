# -*- coding:utf-8 -*-

from lis.patient.models import PatientProfile


class PatientRepository(object):
    """Patient DAO to access the model
    """

    def add_new_patient(self, uuid: str) -> PatientProfile:
        profile = PatientProfile()
        profile.uuid = uuid
        profile.save()

        return profile

    def find_by_uuid(self, uuid: str) -> PatientProfile:
        profile = PatientProfile.collection.filter('uuid', '==', uuid).get()

        return profile
