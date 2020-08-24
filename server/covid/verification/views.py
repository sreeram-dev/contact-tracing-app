# -*- coding:utf-8 -*-


from flask import flash, request, url_for, redirect, render_template
from flask import jsonify
from flask.views import MethodView

from covid.app import app
from covid.verification.services import RegistrationService


class IndexView(MethodView):

    def get(self):
        return jsonify({
            'success': True,
            'message': 'Welcome to covidguard API'
        })


class RegistrationView(MethodView):
    service = RegistrationService()

    def post(self):
        try:
            self.service.validate_request(request)
        except Exception as e:
            data = {
                'success': False,
                'message': str(e),
            }
            return data

        token = self.service.register_uuid(request)

        data = {
            'success': True,
            'message': 'The UUID has been successfully registered',
            'token': token,
        }

        return data
