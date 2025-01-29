
#ifndef MSGSERVICE_H
#define MSGSERVICE_H

#include "Arduino.h"

class Msg
{
private:
    String content;

public:
    Msg(String content)
    {
        this->content = content;
    }

    String getContent()
    {
        return content;
    }
};

class Pattern
{
public:
    virtual boolean match(const Msg &m) = 0;
};

class MsgServiceClass
{

public:
    Msg *currentMsg;
    bool msgAvailable;

    void init();

    bool isMsgAvailable();
    Msg *receiveMsg();

    bool isMsgAvailable(Pattern &pattern);
    Msg *receiveMsg(Pattern &pattern);

    void sendMsg(const String &msg);
};

extern MsgServiceClass MsgService;

#endif
