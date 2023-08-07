import React, {useState, useEffect, useRef} from "react";
import SideMenu from "components/SideMenu";
import io from 'socket.io-client';

import LeftArrowIcon from "/public/images/icon-left-arrow.svg"
import ReflectIcon from "/public/images/icon-reflect.svg"
import CameraIcon from "/public/images/icon-camera.svg"
import MicIcon from "/public/images/icon-mic.svg"
import MoreIcon from "/public/images/icon-more-dot.svg"
import PlayThumbnail from "/public/images/room-playlist-thumbnail.png"
import PlayIcon from "/public/images/icon-play.svg"
import ReflectHoverIcon from "/public/images/icon-hover-reflect.svg"
import MicHoverIcon from "/public/images/icon-hover-mic.svg"
import CameraHoverIcon from "/public/images/icon-hover-camera.svg"
import MoreDotHoverIcon from "/public/images/icon-hover-more-dot.svg"

import { useRecoilState } from "recoil";
import { LanguageState } from "states/states";

import Image from "next/image"
import Link from "next/link"
import { useRouter } from "next/router";

import { axiosMusic, axiosUser, axiosDance } from "apis/axios";
import axios from "axios";
import { accessTokenState, refreshTokenState, idState, nicknameState, profileImgState, rankNameState } from "states/states";

const Hostroom = () => {
    const pc_config = {
        iceServers: [
            // {
            //   urls: 'stun:[STUN_IP]:[PORT]',
            //   'credentials': '[YOR CREDENTIALS]',
            //   'username': '[USERNAME]'
            // },
            {
                urls: 'stun:stun.l.google.com:19302',
            },
        ],
    };
    const SOCKET_SERVER_URL = 'http://localhost:4002';
    const socketRef = useRef<SocketIOClient.Socket>();
    const [accessToken, setAccessToken] = useRecoilState(accessTokenState);
    const [refreshToken, setRefreshToken] = useRecoilState(refreshTokenState);
    const [id, setId] = useRecoilState(idState);
    const [roomName, setRoomName] = useState<any>();

    const [lang, setLang] = useRecoilState(LanguageState);
    const [reflect, setReflect] = useState(false);
    const [mic, setMic] = useState(false);
    const [camera, setCamera] = useState(false);
    const [moredot, setMoredot] = useState(false);
    const [nickname, setNickname] = useRecoilState(nicknameState);
    const [profileImg, setProfileImg] = useRecoilState(profileImgState);
    const [rankname, setRankname] = useRecoilState(rankNameState);
    const [musics, setMusics] = useState<any>();
    const router = useRouter();
    const [roomTitle, setRoomTitle] = useState(router.query.roomName);
    const reflectHover = () => {
        setReflect(true);
    }
    const reflectLeave = () => {
        setReflect(false);
    }
    const micHover = () => {
        setMic(true);
    }
    const micLeave = () => {
        setMic(false);
    }
    const cameraHover = () => {
        setCamera(true);
    }
    const cameraLeave = () => {
        setCamera(false);
    }
    const moreDotHover = () => {
        setMoredot(true);
    }
    const moreDotLeave = () => {
        setMoredot(false);
    }

    const playMusic = (musicId : number) => {
        socketRef.current.emit("playMusic", musicId, roomName);
        alert("새로운 곡이 재생됩니다.");
    }

    const finishRandomPlay = () => {
        socketRef.current.emit("finish", roomName);
        alert("랜덤플레이댄스가 종료되었습니다.");
    }

    useEffect(() => {    
        socketRef.current = io.connect(SOCKET_SERVER_URL);

        axios.post("http://52.78.93.184:8080/api/user/login",{
            id: "ssafy",
            password: "ssafy"
        }).then((data) => {
            if(data.data.message === "로그인 완료"){
                setAccessToken(data.data.data.tokens.accessToken);
                setRefreshToken(data.data.data.tokens.refreshToken);
                setId(id);
                setNickname(data.data.data.userInfo.nickname);
                setProfileImg(data.data.data.userInfo.profileImg);
                setRankname(data.data.data.userInfo.rankName);
            }
        })

        axios.get("http://52.78.93.184:8080/api/music",{
            params:{
                keyword: "",
            },
            headers:{
                Authorization: `Bearer ${accessToken}`,
            }
        }).then((data) => {
            if(data.data.message === "노래 목록 조회 완료"){
                setMusics(data.data.data);
            }
        }).then(() => {
            axiosDance.get('',{
                params: {
                    progressType: "ALL",
                }
            }).then((data) => {
                console.log(data);
                if(data.data.message === "참여 가능한 랜덤 플레이 댄스 목록 조회 완료"){
                    for(let i=0; i<data.data.data.length; i++) {
                        if(data.data.data[i].title === roomTitle){
                            setRoomName(data.data.data[i].randomDanceId);
                        }
                    }
                }
            })
        })
    }, [])
    return(
        <>
            <div className="practiceroom-wrap">
                <SideMenu/>
                <div className="practice-video-wrap">
                    <div className="practice-title">
                        <div className="pre-icon">
                            <Link href="/"><Image src={LeftArrowIcon} alt=""/></Link>
                        </div>
                        <div className="room-title">
                            <h3>보이넥스트도어 - One and Only</h3>
                            <span>2013년 7월 3일</span>
                        </div>
                    </div>

                    <div className="video-content">
                        <div className="my-video">
                            <video src=""></video>
                        </div>
                        <div className="yours-video">
                            <ul>
                                <li>
                                    <video src=""></video>
                                </li>
                                <li>
                                    <video src=""></video>
                                </li>
                                <li>
                                    <video src=""></video>
                                </li>
                                <li>
                                    <video src=""></video>
                                </li>
                            </ul>
                        </div>
                        <div className="control-wrap">
                            <ul>
                                <li onMouseEnter = {reflectHover} onMouseLeave = {reflectLeave}>
                                    <button>
                                        {reflect ? <Image src={ReflectHoverIcon} alt=""/> : <Image src={ReflectIcon} alt=""/>}
                                    </button>
                                </li>
                                <li onMouseEnter = {micHover} onMouseLeave = {micLeave}>
                                    <button>
                                        {mic ? <Image src={MicHoverIcon} alt=""/> : <Image src={MicIcon} alt=""/>}
                                    </button>
                                </li>
                                <li onClick={finishRandomPlay}><button className="exit-button">{lang==="en" ? "End Practice" : lang==="cn" ? "结束练习" : "연습 종료하기" }</button></li>
                                <li onMouseEnter = {cameraHover} onMouseLeave = {cameraLeave}>
                                    <button>
                                        {camera ? <Image src={CameraHoverIcon} alt=""/> : <Image src={CameraIcon} alt=""/>}
                                    </button>
                                </li>
                                <li onMouseEnter = {moreDotHover} onMouseLeave = {moreDotLeave}>
                                    <button>
                                        {moredot ? <Image src={MoreDotHoverIcon} alt=""/> : <Image src={MoreIcon} alt=""/>}
                                    </button>
                                </li>
                            </ul>
                        </div>
                    </div>
                    
                </div>
                <div className="musiclist">
                    <div className="musiclist-title">
                        <h3>{lang==="en" ? "List of practice rooms" : lang==="cn" ? "练习室列表" : "연습실 목록" }</h3>
                        <span>{musics?.length}</span>
                    </div>
                    <div className="musiclist-content">
                        <ul>
                            {musics?.map((music:any, index:any) => {
                                return(
                                    <li key={index}>
                                        <div className="flex-wrap">
                                            <div className="musiclist-content-thumbnail">
                                                <Image src={PlayThumbnail} alt=""/>
                                            </div>
                                            <div className="musiclist-content-info">
                                                <h4>{music.title}</h4>
                                                <span>{music.artist}</span>
                                            </div>
                                        </div>
                                        <div className="musiclist-content-control-icon">
                                            <span onClick={() => playMusic(music.musicId)}><Image src={PlayIcon} alt=""/></span>
                                        </div>
                                    </li>
                                )
                            })}
                        </ul>
                    </div>
                </div>
            </div>
        </>
    )
}

export default Hostroom;