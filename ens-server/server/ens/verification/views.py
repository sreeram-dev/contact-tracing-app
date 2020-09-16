# -*- coding:utf-8 -*-


from flask import flash, request, url_for, redirect, render_template
from flask import jsonify
from flask.views import MethodView

from ens.app import app
from ens.verification.services import RegistrationService


class IndexView(MethodView):

    def get(self):
        return jsonify({
            'success': True,
            'message': 'Welcome to covidguard Diagnosis Key Server API'
        }), 200


class WarmupView(MethodView):

    def get(self):
        return jsonify({
            'success': True,
            'message': 'Warmup Request'
        }), 200


class RegistrationView(MethodView):
    service = RegistrationService()

    def post(self):
        try:
            self.service.validate_request(request)
        except Exception as e:
            data = {
                'code': 400,
                'name': e.__class__.__name__,
                'description': str(e)
            }
            return jsonify(data), 400

        token = self.service.register_uuid(request)

        data = {
            'success': True,
            'message': 'The UUID has been successfully registered',
            'token': token,
        }

        return jsonify(data), 200
