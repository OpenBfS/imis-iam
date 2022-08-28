<#import "template.ftl" as layout>
<@layout.emailLayout>
${kcSanitize(msg("extendAccountValidityBody", username, remainingTime))?no_esc}
</@layout.emailLayout>