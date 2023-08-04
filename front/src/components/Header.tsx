import React, {useEffect, useState} from "react"
import Link from "next/link"

import { useRecoilState } from "recoil";
import { LanguageState, nicknameState, accessTokenState, refreshTokenState, idState, profileImgState, rankNameState } from "states/states";
import { useRouter } from "next/router";

const Header = () => {
    const [lang, setLang] = useRecoilState(LanguageState);
    const [nickname, setNickname] = useRecoilState(nicknameState);
    const [accessToken, setAccessToken] = useRecoilState(accessTokenState);
    const [refreshToken, setRefreshToken] = useRecoilState(refreshTokenState);
    const [id, setId] = useRecoilState(idState);
    const [profileImg, setProfileImg] = useRecoilState(profileImgState);
    const [rankname, setRankname] = useRecoilState(rankNameState);
    const [nav, setNav] = useState<any>(<ul>
		<li><Link href="/login">{lang === "en" ? "LOGIN" : lang === "cn" ? "登陆" : "로그인"}</Link></li>
		<li><Link href="/signup">{lang === "en" ? "SIGNUP" : lang === "cn" ? "注册会员" : "회원가입"}</Link></li>
	</ul>);

    const router = useRouter();

    const signout = () => {
        alert("로그아웃 되었습니다.");
        setAccessToken("");
        setRefreshToken("");
        setNickname("");
        setId("");
        setProfileImg("");
        setRankname("");
        router.push('/');
    }

    useEffect(() => {
		if(nickname != ""){
			setNav(<ul>
				<li onClick={signout}>{lang === "en" ? "SIGNOUT" : lang === "cn" ? "注销" : "로그아웃"}</li>
				<li><Link href="/mypage">{lang === "en" ? "Mypage" : lang === "cn" ? "我的页面" : "마이페이지"}</Link></li>
			</ul>);
		}else if(nickname == ""){
			setNav(
				<ul>
					<li><Link href="/login">{lang === "en" ? "LOGIN" : lang === "cn" ? "登陆" : "로그인"}</Link></li>
					<li><Link href="/signup">{lang === "en" ? "SIGNUP" : lang === "cn" ? "注册会员" : "회원가입"}</Link></li>
				</ul>
			);
		}
	}, [])

    return (
        <>
            <header className="header">
                <div className="block-margin">
                    <div className="logo">
                        <h1><Link href="/">STEP UP</Link></h1>
                    </div>
                    <nav>
                        <ul>
                            <li><h2><Link href="/randomplay/list">{lang==="en" ? "Random play" : lang==="cn" ? "随机播放" : "랜덤플레이" }</Link></h2></li>
                            <li><h2><Link href="/notice/list">{lang==="en" ? "Community" : lang==="cn" ? "公社" : "커뮤니티" }</Link></h2></li>
                            <li><h2><Link href="/playlist/list">{lang==="en" ? "New song" : lang==="cn" ? "新歌申请" : "신곡신청" }</Link></h2></li>
                            <li><h2><Link href="/practiceroom">{lang==="en" ? "Practice room" : lang==="cn" ? "进入练习室" : "연습실입장" }</Link></h2></li>
                        </ul>
                    </nav>
                    <div className="login-wrap">
                        {nav}
                    </div>
                </div>
            </header>

        </>
    )
}

export default Header;