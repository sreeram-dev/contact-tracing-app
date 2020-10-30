# -*- coding:utf-8 -*-

from flask import flash, request, url_for, redirect, render_template

from flask import jsonify
from flask.views import MethodView

from lis.patient.services import PatientService
from lis.patient.errors import ValidationError


class RegistrationView(MethodView):
    """
    """
    patient_service = PatientService()

    def _validate_request(self, request):
        """Validate the request
        """

        uuid = request.form.get('uuid', None)
        if uuid is None:
            raise KeyError('UUID param is required')

        if not (len(uuid) >= 16 and len(uuid) <= 32):
            raise ValueError(
                'UUID Format is wrong - should be 16-32 characters')

        if self.patient_service.find_by_uuid(uuid):
            raise ValueError('UUID already exists uuid: ' + uuid)

    def post(self):
        """
        """

        try:
            self._validate_request(request)
        except ValueError as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }

            return jsonify(data), 400

        uuid = request.form.get('uuid')

        profile = self.patient_service.register_uuid(uuid)

        data = {
            'success': True,
            'message': 'Patient Successfully registered',
            'profile': profile.to_dict()
        }

        return jsonify(data), 200


class DiagnosisView(MethodView):
    """Set if the patient is diagnoised
    """
    patient_service = PatientService()

    def _validate_post_request(self, request):
        """Validate the request
        """
        if not request.is_json:
            raise ValueError('JSON Request is expected')

        json_data = request.get_json()
        uuid = json_data.get('uuid', None)

        if uuid is None:
            raise KeyError('UUID param is required')

        if not (len(uuid) >= 16 and len(uuid) <= 32):
            raise ValueError(
                'UUID Format is wrong - should be 16-32 characters')

        profile = self.patient_service.find_by_uuid(uuid)
        if not profile:
            raise ValueError('UUID does not exist. uuid: ' + uuid)

    def post(self):
        """Set is-positive or is-recovered for the user
        """

        try:
            self._validate_post_request(request)
        except (KeyError, ValueError) as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }
            return jsonify(data), 400

        json_data = request.get_json()
        uuid = json_data.get('uuid')

        recovered_status = json_data.get('is-recovered', None)
        positive_status = json_data.get('is-positive', None)

        try:
            self.patient_service.set_diagnosis_status(
                uuid, recovered_status, positive_status)
        except ValidationError as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }
            return jsonify(data), 400

        profile = self.patient_service.find_by_uuid(uuid)

        data = {
            'success': True,
            'message': 'Patient has been successfully diagnosed',
            'profile': profile.to_dict()
        }

        return jsonify(data), 200

    def _validate_get_request(self, request):
        """
        """
        uuid = request.args.get('uuid', None)

        if uuid is None:
            raise KeyError('UUID param is required')

        if not (len(uuid) >= 16 and len(uuid) <= 32):
            raise ValueError(
                'UUID Format is wrong - should be 16-32 characters')

        profile = self.patient_service.find_by_uuid(uuid)
        if not profile:
            raise ValueError('UUID does not exist. uuid: ' + uuid)

    def get(self):
        """Set is-positive or is-recovered for the user
        """

        try:
            self._validate_get_request(request)
        except (KeyError, ValueError) as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }
            return jsonify(data), 400

        uuid = request.args.get('uuid')
        profile = self.patient_service.find_by_uuid(uuid)

        try:
            self.patient_service.set_diagnosis_status(
                uuid, False, True)
        except ValidationError as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }
            return jsonify(data), 400

        profile = self.patient_service.find_by_uuid(uuid)

        data = {
            'success': True,
            'message': 'Patient has been successfully marked positive',
            'profile': profile.to_dict()
        }

        return jsonify(data), 200


class StatusView(MethodView):
    """Get the status of the patient
    """
    patient_service = PatientService()

    def _validate_request(self, request):
        """Validate the request
        """
        uuid = request.args.get('uuid', None)
        if uuid is None:
            raise KeyError('UUID param is required')

        if not (len(uuid) >= 16 and len(uuid) <= 32):
            raise ValueError(
                'UUID Format is wrong - should be 16-32 characters')

        if not self.patient_service.find_by_uuid(uuid):
            raise ValueError('UUID does not exist. uuid: ' + uuid)

    def get(self):
        """
        """
        try:
            self._validate_request(request)
        except (KeyError, ValueError) as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }
            return jsonify(data), 400

        uuid = request.args.get('uuid')

        profile = self.patient_service.find_by_uuid(uuid)

        data = {
            'success': True,
            'profile': profile.to_dict()
        }

        return jsonify(data), 200
