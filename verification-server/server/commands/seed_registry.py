# -*- coding:utf-8 -*-
"""Seed registry to seed the initial database
"""

# -*- coding:utf-8 -*-


import os
import json

from flask_script import Command

from covid.app import app
from covid.providers.db import FireStoreClient
from covid.constants import TOKEN_COLLECTION
from covid.utils import get_collection_name


class SeedFirestore(Command):
    """Command to initialise firestore collections
       Initialize the following collections -
       1. tokens
       2. tans
       3. rpis
    """

    def run(self):
        app.logger.info('Firestore collections are created automatically, no \
                need to write them')
