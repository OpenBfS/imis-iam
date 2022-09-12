<#import "template.ftl" as layout>
<@layout.emailLayout>
${kcSanitize(msg("accountExpiredBodyHtml", username))?no_esc}
</@layout.emailLayout>