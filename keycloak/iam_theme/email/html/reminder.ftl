<#import "template.ftl" as layout>
<@layout.emailLayout>
${kcSanitize(msg("reminderBodyHtml", topic))?no_esc}
</@layout.emailLayout>