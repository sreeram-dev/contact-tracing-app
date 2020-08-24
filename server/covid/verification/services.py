# -*- coding:utf-8 -*-

import uuid

from covid.verification.repo import TokenRepository


class RegistrationService(object):
    """Service to handle registration of tokens
    """
    token_repo = TokenRepository()

    def validate_request(self, request):
        """Validate request
        """

        if not request.form.get('uuid', None):
            raise ValueError('UUID not present')

        uuid = request.form.get('uuid')

        if len(uuid) != 16:
            raise ValueError('UUID length not equal to 16')

        if self.token_repo.find_by_uuid(uuid):
            raise ValueError('UUID already exists')


    def register_uuid(self, request):
        uuid = request.form.get('uuid')
        token = uuid.uuid4().hex
        self.token_repo.insert(uuid, token)

        return token
