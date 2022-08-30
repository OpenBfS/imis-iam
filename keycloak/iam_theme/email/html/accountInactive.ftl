<#import "template.ftl" as layout>
<@layout.emailLayout>
${kcSanitize(msg("accountInactiveBodyHtml", users))?no_esc}
</@layout.emailLayout>