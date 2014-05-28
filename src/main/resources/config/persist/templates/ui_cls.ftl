/*********************************************************************************
 * This object represents a ${name}.
 * generated: ${date?datetime}
 */
var ${name} = function(attrbs) {
    Entity.call(this, attrbs);
    this.createAvatar();
};

${name}.prototype = new Entity();

${name}.prototype.createAvatar = function() {
    var avatar = $("<tr/>");
<#list fields as field>
    avatar.append($("<td>" + this.${field} + "</td>"));
</#list>
    this.setAvatar(avatar);
};