# -*- coding:utf-8 -*-


from lis.app import APP_MODE
from urllib.parse import urlparse, urljoin


def get_collection_name(collection):
    """Get collection name
    """
    mode = APP_MODE.lower()
    if mode.lower() == 'prod':
        return collection
    return mode.lower() + '_' + collection


def is_safe_url(target, request):
    ref_url = urlparse(request.host_url)
    test_url = urlparse(urljoin(request.host_url, target))
    return test_url.scheme in ('http', 'https') and \
        ref_url.netloc == test_url.netloc


def validate_host(host):
    pass
