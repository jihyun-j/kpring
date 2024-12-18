import { useNavigate } from "react-router";
import LoginForm from "../organisms/LoginForm";
import Button from "../atoms/Button";

const LoginTemplate = () => {
  const navigate = useNavigate();
  return (
    <div>
      <LoginForm />
      <Button onClick={() => navigate("/join")} color="red">
        회원가입
      </Button>
    </div>
  );
};

export default LoginTemplate;
