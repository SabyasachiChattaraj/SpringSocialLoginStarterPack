<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<portlet:actionURL var="twitterLogin">
	<portlet:param name="login" value="twitter"></portlet:param>
</portlet:actionURL>

<form action="<%=twitterLogin%>" method="post">
	<input type="submit" value="Twitter"/>
</form>

<br/>
<portlet:actionURL var="linkedinLogin">
	<portlet:param name="login" value="linkedin"></portlet:param>
</portlet:actionURL>


<form action="<%=linkedinLogin%>" method="post">
	<input type="submit" value="Linkedin"/>
</form>
<br/>
<portlet:actionURL var="facebookLogin">
	<portlet:param name="login" value="facebook"></portlet:param>
</portlet:actionURL>


<form action="<%=facebookLogin%>" method="post">
	<input type="submit" value="Facebook"/>
</form>
<br/>
<portlet:actionURL var="googleLogin">
	<portlet:param name="login" value="google"></portlet:param>
</portlet:actionURL>


<form action="<%=googleLogin%>" method="post">
	<input type="submit" value="Google"/>
</form>


<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>

<script type="text/javascript">
</script>
