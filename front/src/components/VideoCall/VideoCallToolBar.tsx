// TODO : 비디오 툴바
import { BsArrowDown } from "react-icons/bs";
import { HiCamera } from "react-icons/hi";
import { IoMic } from "react-icons/io5";
import { RiScreenshotFill } from "react-icons/ri";

const VideoCallToolBar = () => {
  return (
    <div className="bg-white w-52 h-12 rounded-3xl flex items-center justify-around px-2">
      <div className="pointer">
        <IoMic />
        <BsArrowDown className="rounded-sm" />
      </div>
      <div className="pointer">
        <HiCamera />
        <BsArrowDown className="rounded-sm" />
      </div>
      <div>
        <RiScreenshotFill />
      </div>
    </div>
  );
};

export default VideoCallToolBar;
