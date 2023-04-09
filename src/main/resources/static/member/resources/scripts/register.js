const form = window.document.getElementById('form');

//select 된 도메인 값 가져오는 함수
const domainSelected = document.getElementById('domainSelect');

form.querySelector('[rel = "registerSend"]').addEventListener('click', () => {

    if (!form['emailSend'].disabled || !form['emailVerify'].disabled) {
        alert('이메일 인증을 완료해주세요.');

        return;
    }
    if (form['password'].value === '' || form['passwordCheck'].value === '') {
        alert('비밀번호 작성을 완료해주세요.');
        form['password'].focus();
        return;
    }

    if(form['password'].value.length <8 || form['passwordCheck'].value.length <8){
        alert('8자 이상의 비밀번호를 입력해주세요.');
        form['password'].focus();
        return;
    }

    if (form['password'].value !== form['passwordCheck'].value) {
        alert('비밀번호가 일치하지 않습니다. 비밀번호를 확인해주세요.');
        form['password'].focus();
        return;
    }

    if (form['name'].value === '' || form['name'].value.length <2) {
        alert('이름 작성을 완료해주세요.');
        form['name'].focus();
        return;
    }

    if (form['nickname'].value === '' || form['nickname'].value.length <2) {
        alert('닉네임 작성을 완료해주세요.');
        form['nickname'].focus();
        return;
    }

    if (form['contact'].value === '') {
        alert('연락처 작성을 완료해주세요.');
        form['contact'].focus();
        return;
    }

    if (form['addressPostal'].value === '' || form['addressPrimary'].value === '' || form['addressSecondary'].value === '') {
        alert('주소 작성을 완료해 주세요.');
        form['addressSecondary'].focus()
        return;
    }

    if (!form['onAgree1'].checked || !form['onAgree2'].checked || !form['onAgree3'].checked ) {
        alert('(필수) 약관동의 항목에 대해 동의해주세요.');
        return;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', form['email'].value + '@' + domainSelected.options[document.getElementById('domainSelect').selectedIndex].value);
    formData.append('code', form['emailAuthCode'].value);
    formData.append('salt', form['emailAuthSalt'].value);
    formData.append('password', form['password'].value);
    formData.append('nickname', form['nickname'].value);
    formData.append('name', form['name'].value);
    formData.append('contact', form['contact'].value);
    formData.append('addressPostal', form['addressPostal'].value);
    formData.append('addressPrimary', form['addressPrimary'].value);
    formData.append('addressSecondary', form['addressSecondary'].value);

    xhr.open('POST', './register');
    xhr.onreadystatechange = () => {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status >= 200 && xhr.status < 300) {
                const responseObject = JSON.parse(xhr.responseText);
                switch (responseObject['result']) {
                    case 'success':
                        window.location.href = '/member/registerCom';
                        break;
                    case  'email_not_verified':
                        alert('이메일 인증이 완료되지 않았습니다.');
                        break;
                    case 'overlap_nickname':
                        alert('이미 등록된 닉네임입니다.');
                        break;
                    case 'overlap_contact':
                        alert('이미 등록된 연락처입니다.');
                        break;
                    default:
                        alert('알 수 없는 이유로 회원가입이 완료하지 못하였습니다. 잠시 후 다시 시도해주세요.');
                        break;
                }
            } else {
                alert('서버와 통신하지 못하였습니다. 잠시 후 다시 시도해주세요.');
            }
        }
    }
    xhr.send(formData);

});

form['addressFind'].addEventListener('click', () => {
    new daum.Postcode({
        oncomplete: e => {
            form.querySelector('[rel="addressFindPanel"]').classList.remove('visible');
            form['addressPostal'].value = e['zonecode'];
            form['addressPrimary'].value = e['address'];
            form['addressSecondary'].value = '';
            form['addressSecondary'].focus();
        }
    }).embed(form.querySelector('[rel="addressFindPanelDialog"]'));
    form.querySelector('[rel="addressFindPanel"]').classList.add('visible');
});

form.querySelector('[rel="addressFindPanel"]').addEventListener('click', () => {
    form.querySelector('[rel="addressFindPanel"]').classList.remove('visible');
});

form['emailSend'].addEventListener('click', () => {
    if (form['email'].value === '') {
       alert('이메일을 입력해주세요.');
        form['email'].focus();
        return;
    }

    if (domainSelected.value === '선택해주세요'){
        alert('이메일 도메인을 선택해주세요');
        return;
    }


    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', form['email'].value + '@' + domainSelected.options[document.getElementById('domainSelect').selectedIndex].value);
    // append를 통해 여러개의 값을 formData라는 것에 실어서 send할 수 있다.
    xhr.open('POST', './email');
    // 경로도 유심히 봐야한다. ./은
    xhr.onreadystatechange = () => {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status >= 200 && xhr.status < 300) {
                const responseObject = JSON.parse(xhr.responseText);
                switch (responseObject['result']) {
                    case 'success':
                        alert('인증 번호를 전송하였습니다. 전송된 인증 번호는 5분간만 유효합니다.');
                        form['email'].setAttribute('disabled', 'disabled');
                        form['emailSend'].setAttribute('disabled', 'disabled');
                        domainSelected.setAttribute('disabled', 'disabled');
                        form['emailAuthCode'].removeAttribute('disabled');
                        document.getElementById('emailCheckContainer').classList.add('visible');
                        form['emailAuthCode'].focus();
                        form['emailAuthSalt'].value = responseObject['salt'];
                        form['emailVerify'].removeAttribute('disabled');
                        break;
                    /*
                    성공시 1. 인증번호 확인란 disabled 풀어주기
                    2. 인증번호 전송란 disabled 걸어주기
                    3. innerText란 인증번호를 전송했다라고 바꾸어 주어야 함.
                    */

                    case 'email_duplicated':
                        alert('해당 이메일은 이미 사용 중입니다.');
                        form['email'].focus();
                        form['email'].select();
                        break;
                    default:
                        alert('알 수 없는 이유로 인증 번호를 전송하지 못 하였습니다. 잠시 후 다시 시도해 주세요.');
                        form['email'].focus();
                        form['email'].select();
                }
                // console.log('이게');
                // console.log(xhr.responseText);
                // console.log('이렇게 변함');
                // console.log(JSON.parse(xhr.responseText));
                // console.log('----------------');
                // console.log(responseObject['result']);
                // console.log(responseObject['salt']);
                // Json객체로 받아온 "문자열"을 Js에서 오브젝트타입으로 바꾸기 위해서 JSON.parse를 사용하였다.
            } else {
               alert('서버와 통신하지 못하였습니다.잠시후 다시 시도해 주세요.');
            }
        }
    }
    xhr.send(formData);
});

// email 인증을 클릭시 -------------------------------------------

form['emailVerify'].addEventListener('click', () => {
    if (form['emailAuthCode'].value === '') {
        alert('인증번호를 입력해 주세요.');
        form['emailAuthCode'].focus();
        return;
    }
    if (!new RegExp('^(\\d{6})$').test(form['emailAuthCode'].value)) {
        alert('올바른 인증 번호를 입력해 주세요.');
        form['emailAuthCode'].focus();
        form['emailAuthCode'].select();
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', form['email'].value + '@' + domainSelected.options[document.getElementById('domainSelect').selectedIndex].value);
    formData.append('code', form['emailAuthCode'].value);
    formData.append('salt', form['emailAuthSalt'].value);
    xhr.open('PATCH', 'email');
    xhr.onreadystatechange = () => {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status >= 200 && xhr.status < 300) {
                const responseObject = JSON.parse(xhr.responseText);
                switch (responseObject['result']) {
                    case 'expired':
                        alert('인증 정보가 만료되었습니다. 다시 시도해 주세요.');
                        form['email'].removeAttribute('disabled');
                        form['email'].focus();
                        form['email'].select();
                        form['emailSend'].removeAttribute('disabled');
                        form['emailAuthCode'].value = '';
                        form['emailAuthCode'].setAttribute('disabled', 'disabled');
                        form['emailAuthSalt'].value = '';
                        form['emailVerify'].setAttribute('disabled', 'disabled');
                        break;
                    case 'success':
                       alert('이메일이 정상적으로 인증되었습니다.');
                        form['emailAuthCode'].setAttribute('disabled', 'disabled');
                        form['emailVerify'].setAttribute('disabled', 'disabled');
                        form['password'].focus();
                        break;
                    default:
                        alert('인증번호가 올바르지 않습니다.');
                        form['emailAuthCode'].focus();
                        form['emailAuthCode'].select();
                }
            } else {
               alert('서버와 통신하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
            }
        }
    }
    xhr.send(formData);
});












