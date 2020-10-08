# -*- coding:utf-8 -*-


class TANValidationError(Exception):

    def __init__(self, message, error_code=400):

        # Call the base class constructor with the parameters it needs
        super().__init__(message)
        self.error_code = 400
