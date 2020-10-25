# -*- coding:utf-8 -*-

from flask import flash, request, url_for, redirect, render_template

from flask import jsonify
from flask.views import MethodView

from lis.utils import validate_host
from lis.patient.errors import PatientNotFound

from lis.consent.services import ConsentService
from lis.consent.errors import ConsentNotFound


class GrantConsentView(MethodView):
    """
    """
    consent_service = ConsentService()

    def _validate_request(self, request):
        """Validate the request
        """

        uuid = request.form.get('uuid', None)
        if uuid is None:
            raise KeyError('UUID param is required')

        if not (len(uuid) >= 16 and len(uuid) <= 32):
            raise ValueError(
                'UUID Format is wrong - should be 16-32 characters')

        host = request.form.get('host', None)
        if host:
            validate_host(host)

    def post(self):

        try:
            self._validate_request(request)
        except (ValueError, KeyError) as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }

            return jsonify(data), 400

        uuid = request.form.get('uuid')
        host = request.form.get('host', None)
        if not host:
            host = request.remote_addr
        try:
            consent = self.consent_service.grant_consent(uuid, host)
        except PatientNotFound as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }

            return jsonify(data), 400

        data = {
            'success': True,
            'message': 'Consent Successfully granted',
            'consent': consent.to_dict()
        }

        return jsonify(data), 200


class RevokeConsentView(MethodView):
    """
    """
    consent_service = ConsentService()

    def _validate_request(self, request):
        """Validate the request
        """

        uuid = request.form.get('uuid', None)
        if uuid is None:
            raise KeyError('UUID param is required')

        if not (len(uuid) >= 16 and len(uuid) <= 32):
            raise ValueError(
                'UUID Format is wrong - should be 16-32 characters')

        host = request.form.get('host', None)
        if host:
            validate_host(host)

    def post(self):

        try:
            self._validate_request(request)
        except ValueError | KeyError as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }

            return jsonify(data), 400

        uuid = request.form.get('uuid')
        host = request.form.get('host', None)
        if not host:
            host = request.remote_addr

        try:
            consent = self.consent_service.revoke_consent(uuid, host)
        except (PatientNotFound, ConsentNotFound) as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }

            return jsonify(data), 400

        data = {
            'success': True,
            'message': 'Consent Successfully Revoked',
            'consent': consent.to_dict()
        }

        return jsonify(data), 200


class AuthenticateConsentView(MethodView):
    """
    """
    consent_service = ConsentService()

    def _validate_request(self, request):
        """Validate the request
        """

        uuid = request.args.get('uuid', None)
        if uuid is None:
            raise KeyError('UUID param is required')

        if not (len(uuid) >= 16 and len(uuid) <= 32):
            raise ValueError(
                'UUID Format is wrong - should be 16-32 characters')

        host = request.form.get('host', None)
        if host:
            validate_host(host)

    def get(self):

        try:
            self._validate_request(request)
        except ValueError | KeyError as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }

            return jsonify(data), 400

        uuid = request.args.get('uuid', None)
        host = request.args.get('host', None)
        if not host:
            host = request.remote_addr

        try:
            consent = self.consent_service.authenticate_consent(uuid, host)
        except (PatientNotFound, ConsentNotFound) as e:
            data = {
                'code': 400,
                'name': 'LIS/PatientService',
                'description': str(e)
            }

            return jsonify(data), 400

        data = {
            'success': True,
            'message': 'Consent is valid',
            'consent': consent.to_dict()
        }

        return jsonify(data), 200
