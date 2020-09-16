# -*- coding:utf-8 -*-
"""Seed registry to seed the initial database
"""

# -*- coding:utf-8 -*-


import os
import json

from flask_script import Command

from ens.app import app
from ens.providers.db import FireStoreClient
from ens.constants import TOKEN_COLLECTION
from ens.utils import get_collection_name


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
