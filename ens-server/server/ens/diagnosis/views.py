# -*- coding: utf-8 -*-

import json
import traceback

from flask import flash, request, url_for, redirect, render_template

from flask import jsonify
from flask.views import MethodView

from ens.app import app
from ens.diagnosis.services import DiagnosisKeyService


class UploadView(MethodView):
    """
    """
    diagnosis_service = DiagnosisKeyService()

    def _validate_upload_request(self, request):

        if not request.is_json:
            data = {
                'code': 400,
                'name': 'DiagnosisUploadService',
                'description': 'Malformed Request, Expecting application/json'
            }

            return jsonify(data), 400

        json_data = request.get_json()
        tan = json_data.get('tan', None)
        if tan is None:
            data = {
                'code': 400,
                'name': 'DiagnosisUploadService',
                'description': 'TAN is not given in the request payload'
            }

            return jsonify(data), 400

        if json_data.get('teks') is None:
            data = {
                'code': 400,
                'name': 'DiagnosisUploadService',
                'description': 'TAN is not valid.'
            }

            return jsonify(data), 400

    def post(self):
        """
        """
        err_response = self._validate_upload_request(request)

        if err_response is not None:
            return err_response

        json_data = request.get_json()
        data_payload = json.dumps(json_data)

        data = self.diagnosis_service.call_verify_tan(
            json_data.get('tan', None))

        if not data.get('is-valid', False):
            data = {
                'code': 400,
                'name': 'DiagnosisUploadService',
                'description': data['description']
            }

            return jsonify(data), 400

        try:
            self.diagnosis_service.upload_teks(json_data['teks'])
        except Exception as e:
            data = {
                'code': 500,
                'name': 'UploadService',
                'description': 'Upload Failed: ' + str(e),
                'traceback': traceback.format_exc()
            }
            return jsonify(data), 400
        return jsonify({'success': True, 'message': 'Upload Successful'}), 200


class DownloadView(MethodView):
    diagnosis_service = DiagnosisKeyService()

    def get(self):
        """
        """
        teks = self.diagnosis_service.download_teks()
        return jsonify({'teks': teks}), 200
