<#if task.descriptions[0]??>
  <div>
	Description: ${task.descriptions[0].text}
  </div>
  <hr/>
</#if>
<div>
	User: ${userObject.eppn}
</div>
<hr/>
<div>
	User-ID: ${userId}
</div>