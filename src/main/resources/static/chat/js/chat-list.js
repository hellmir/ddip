import {getAllProduct, getAllChatroom, getChatroomByProductId} from "./api.js";

const allChatroom = await getAllChatroom();
let stompClient = '';
let connectStatus = false;
let subscriptions = {};
let focusChatroom = '';

export const getConnect = async () => {
  const allProducts = await getAllProduct();
  const chatList = [];
  if (allProducts.length > 0) {
    const productsId = allProducts.map(e => e.productId);
    if (!connectStatus) connect(productsId);

    for (const e of allProducts) {
      const messageObj = await getChatroomByProductId('suwan', e.productId);
      chatList.push(messageObj);
    }
    if (chatList.length > 0) drawChatList(chatList);
  }
}
getConnect();

function connect(productsId) {
  const socket = new SockJS("/ws-stomp");
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    console.log('frame', frame);
    productsId.forEach(productId => {
      subscribeToProduct(productId);
    })
  });
  connectStatus = true;
}

const subscribeToProduct = (productId) => {
  if (stompClient && stompClient.connected) {
    subscriptions[productId] = stompClient.subscribe(`/room/${productId}`, chatMessage => {
      const messageObj = JSON.parse(chatMessage.body);
      console.log('지금 있기는 하나? ', messageObj)
      //appendMessageTag()
      /*const roomId = document.querySelector(`div[data-product="${messageObj.roomId}"] > p`)
      roomId.style.color = 'red';*/
    })
  }
}

//===========메시지 전송===============
document.querySelector("section.input-div textarea").addEventListener("keydown", e => {
  if (e.keyCode === 13 && !e.shiftKey) {
    e.preventDefault();
    sendMessage(); // 입력창 초기화
    document.querySelector("div.input-div textarea").value = "";
  }
});

// 메시지 태그 생성
const createMessageTag = (LR_className, senderName, message) => {
  const chatLi = document.querySelector("section.chat.format ul li").cloneNode(true);
  chatLi.classList.add(LR_className);
  chatLi.querySelector(".sender span").textContent = senderName;
  chatLi.querySelector(".message span").textContent = message;
  return chatLi;
}

// 메시지 태그 추가
const appendMessageTag = (LR_className, senderName, message) => {
  const chatLi = createMessageTag(LR_className, senderName, message);
  //document.querySelector("section.chat:not(.format) ul").appendChild(chatLi);
  //document.querySelector("section.chat").scrollTop = document.querySelector("section.chat").scrollHeight;
}

function sendMessage() {
  const messageInputEle = document.querySelector('textarea');
  const messageObj = {
    message: messageInputEle.value
  }

  if (messageInputEle.value != '') {
    stompClient.send(`/messages/${focusChatroom}`, {}, JSON.stringify(messageObj));
    messageInputEle.value = '';
  }
}


//===========================================================================================

const drawChatList = (allChatroom) => {
  const chatRoomArea = document.querySelector('.chat-list');
  let html = '';
  if (allChatroom.length === 0) {
    html += `<h3>게시글이 없습니다.</h3>`
  } else {
    for (let chat of allChatroom) {
      if (chat.length !== undefined) {
        html += `<div class="user" data-product="${chat[0].productId}">`;
        html += `<img src="/img/trend/bs-1.jpg" alt="물품사진"/>`
        html += `<div><p>${chat[0].senderNickname}</p>`
        html += `<p>${chat[0].message}</p></div></div>`
      } else {
        html += `<div class="user" data-product="${chat.productId}">`;
        html += `<img src="/img/trend/bs-1.jpg" alt="물품사진"/>`
        html += `<div><p>${chat.senderNickname}</p>`
        html += `<p>${chat.message}</p></div></div>`
      }
    }
  }

  chatRoomArea.innerHTML = html;

  const chatrooms = document.querySelectorAll('.user');
  if (chatrooms.length > 0) {
    chatrooms.forEach(e => {
      e.addEventListener('click', async (e) => {
        let roomId = '';
        let focusStatus = false;
        if (e.target.tagName !== 'DIV') {
          if (e.target.tagName !== 'IMG') {
            const targetEle = e.target.parentElement.parentElement;
            roomId = targetEle.dataset.product;
            addClassName(targetEle);
          } else {
            const targetEle = e.target.parentElement;
            roomId = targetEle.dataset.product;
            addClassName(targetEle);
          }
        } else {
          const targetEle = e.target;
          roomId = targetEle.dataset.product;
          addClassName(targetEle);
        }
        focusChatroom = roomId;
        const messageObj = await getChatroomByProductId('suwan', roomId);
        drawChat(messageObj)
      })
    })
  }
}
drawChatList(allChatroom);

const drawChat = (messageObjs) => {
  const chatRoomEle = document.querySelector('.chat');

  // 기존 메시지 모두 제거 (초기화)
  chatRoomEle.innerHTML = '';
  appendMessageTag();

  messageObjs.forEach(messageObj => {
    console.log('messageObj', messageObj)
  });
};

const addClassName = (targetEle) => {
  const elements = document.querySelector('.chat-list');
  const children = elements.children;
  for (let child of children) child.classList.remove('active')

  targetEle.classList.add('active');
}
