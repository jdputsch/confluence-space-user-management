/**
 * Copyright (c) 2007-2012, Custom Space User Management Plugin Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Custom Space User Management Plugin Development Team
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package csum.confluence.permissionmgmt.service.vo;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.spaces.Space;
import csum.confluence.permissionmgmt.config.CustomPermissionConfigurable;

/**
 * @author Gary S. Weaver
 */
public class ServiceContext {

    private ConfluenceActionSupport confluenceActionSupport;
    private Space space;
    private CustomPermissionConfigurable customPermissionConfigurable;

    public ConfluenceActionSupport getConfluenceActionSupport() {
        return confluenceActionSupport;
    }

    public void setConfluenceActionSupport(ConfluenceActionSupport confluenceActionSupport) {
        this.confluenceActionSupport = confluenceActionSupport;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public CustomPermissionConfigurable getCustomPermissionConfigurable() {
        return customPermissionConfigurable;
    }

    public void setCustomPermissionConfigurable(CustomPermissionConfigurable customPermissionConfigurable) {
        this.customPermissionConfigurable = customPermissionConfigurable;
    }

    /**
     * Convenience method to getText from i18n resource backing the action, assuming it exists
     *
     * @param key - key
     * @return i18n message string if confluenceActionSupport set on context.
     */
    public String getText(String key) {
        String result = key;

        ConfluenceActionSupport cas = getConfluenceActionSupport();
        if (cas != null) {
            result = cas.getText(key);
        }

        return result;
    }

    /**
     * Convenience method to getText from i18n resource backing the action, assuming it exists
     *
     * @param key    - key
     * @param values - values
     * @return i18n message string if confluenceActionSupport set on context.
     */
    public String getText(String key, String[] values) {
        String result = key;

        ConfluenceActionSupport cas = getConfluenceActionSupport();
        if (cas != null) {
            result = cas.getText(key, values);
        }

        return result;
    }
}
