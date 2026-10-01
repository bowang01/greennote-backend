package com.greennote.system.site.service;

import com.greennote.security.AuthPrincipal;
import com.greennote.system.site.controller.vo.SiteConfig;

public interface SiteService {

    SiteConfig current();

    SiteConfig update(SiteConfig request, AuthPrincipal operator);
}
