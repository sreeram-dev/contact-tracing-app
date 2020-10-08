# -*- coding:utf-8 -*-

from flask import flash, request, url_for, redirect, render_template
from flask import jsonify
from flask.views import MethodView

from covid.profile.services import TokenService
from covid.tan.services import TANService
from covid.tan.errors import TANValidationError


class RequestTANView(MethodView):
    """
    """
    tan_service = TANService()
    token_service = TokenService()

    def post(self):
        """Get a tan for the user
        """
        # check if token is formed by get
        token = request.form.get('token')
        if token is None:
            data = {
                'code': 400,
                'name': 'TANService',
                'description': 'Token is not given in the repo'
            }

            return jsonify(data), 400

        try:
            self.token_service.validate_token(token)
        except Exception:
            data = {
                'code': 400,
                'name': 'TANService',
                'description': 'Token does not exist',
            }
            return jsonify(data), 400

        tan_info = self.tan_service.generate_tan_for_token(token)

        data = {
            'success': True,
            'tan': tan_info.get_tan(),
            'expired-at': tan_info.get_expired_at()
        }

        return jsonify(data), 200


class VerifyTANView(MethodView):
    """
    """
    tan_service = TANService()

    def post(self):
        tan = request.form.get('tan')

        try:
            self.tan_service.validate_tan(tan)
        except ValueError as e:
            data = {
                'code': 400,
                'description': str(e),
                'name': 'TANService'
            }
            return jsonify(data), 400
        except TANValidationError as e:
            data = {
                'code': 400,
                'description': str(e),
                'name': 'TANService'
            }
            return jsonify(data), 400

        data = {
            'success': True,
            'tan': tan,
            'is-valid': True
        }

        return jsonify(data), 200
