# How to use

# action:sendMessage

> https://sekiro.virjar.com/invoke?group=wx_robot&action=sendMessage&content=哈哈哈哈&receiverNickname=Blanke&atWechatIds=&isGroup=false&receiverWxId=
> https://sekiro.virjar.com/invoke?group=wx_robot&action=sendMessage&content=叼毛&receiverNickname=&atWechatIds=notify@all&isGroup=true&receiverWxId=20964698150@chatroom

- content:消息内容
- receiverWxId:消息接收者微信id
- receiverNickname:消息接收者微信昵称
- atWechatIds:艾特的微信id，通过逗号分割多个微信Id
- isGroup:只有在群聊中才可以使用@功能
- msgType:消息类型，默认为文本消息，图片为1
- imageUrl:图片下载链接，发送图片必须字段
- md5:可选字段，用于避免重复下载文件


> 发送图片例子：https://sekiro.virjar.com/invoke?group=wx_robot&action=sendMessage&receiverNickname=Blanke&isGroup=false&msgType=1&imageUrl=http://ww4.sinaimg.cn/bmiddle/006APoFYjw1fa6za75onvj30bf0djdgv.jpg

# action:findContactInfo

根据微信昵称查找用户微信Id

> https://sekiro.virjar.com/invoke?group=wx_robot&action=wxIdQuery&nickname=Blanke

# action:ContactList

通信录列表

> https://sekiro.virjar.com/invoke?group=wx_robot&action=ContactList

