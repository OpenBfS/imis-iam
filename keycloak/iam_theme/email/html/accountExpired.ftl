<#import "template.ftl" as layout>
<@layout.emailLayout>
${kcSanitize(msg("accountExpired", username))?no_esc}
</@layout.emailLayout>